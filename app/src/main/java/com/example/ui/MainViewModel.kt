package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DevicePhoneRepository
import com.example.data.PreferencesRepository
import com.example.model.CallLogEntry
import com.example.model.CardProfile
import com.example.model.CompanyItem
import com.example.model.ContactEntry
import com.example.model.ShareSettings
import com.example.model.ShareTab
import com.example.model.WhatsAppAppPreference
import com.example.util.IntentLauncher
import com.example.util.PhoneFormatter
import com.example.util.WhatsAppLaunchResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val hasCallLogPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val isLoading: Boolean = false,
    val activeTab: ShareTab = ShareTab.RECENT_CALLS,
    val searchQuery: String = "",
    val manualNumberInput: String = "",
    val callLogs: List<CallLogEntry> = emptyList(),
    val contacts: List<ContactEntry> = emptyList(),
    val cardProfile: CardProfile = CardProfile(),
    val shareSettings: ShareSettings = ShareSettings(),
) {
    val activeCompany: CompanyItem
        get() = shareSettings.activeCompany

    val effectiveCardProfile: CardProfile
        get() {
            val company = activeCompany
            val isDefaultCompany = company.id == ShareSettings.DEFAULT_COMPANY_ID
            return cardProfile.copy(
                organization = if (isDefaultCompany) cardProfile.organization else company.name,
                websiteUrl = company.websiteUrl.ifBlank { cardProfile.websiteUrl },
                cardShareUrl = company.businessCardUrl.ifBlank { cardProfile.cardShareUrl },
            )
        }

    val lastCalledEntry: CallLogEntry?
        get() = callLogs.firstOrNull()

    val filteredCallLogs: List<CallLogEntry>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return callLogs
            return callLogs.filter { entry ->
                entry.displayName.orEmpty().contains(query, ignoreCase = true) ||
                    entry.phoneNumber.contains(query, ignoreCase = true)
            }
        }

    val filteredContacts: List<ContactEntry>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return contacts
            return contacts.filter { contact ->
                contact.displayName.contains(query, ignoreCase = true) ||
                    contact.phoneNumber.contains(query, ignoreCase = true)
            }
        }
}

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application.applicationContext)
    private val phoneRepo = DevicePhoneRepository(application.applicationContext)

    private val _hasCallLogPerm = MutableStateFlow(phoneRepo.hasCallLogPermission())
    private val _hasContactsPerm = MutableStateFlow(phoneRepo.hasContactsPermission())
    private val _isLoading = MutableStateFlow(false)
    private val _activeTab = MutableStateFlow(ShareTab.RECENT_CALLS)
    private val _searchQuery = MutableStateFlow("")
    private val _manualNumberInput = MutableStateFlow("")
    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(emptyList())
    private val _contacts = MutableStateFlow<List<ContactEntry>>(emptyList())

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        _hasCallLogPerm,
        _hasContactsPerm,
        _isLoading,
        _activeTab,
        _searchQuery,
        _manualNumberInput,
        _callLogs,
        _contacts,
        prefsRepo.cardProfileFlow,
        prefsRepo.shareSettingsFlow,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        MainUiState(
            hasCallLogPermission = values[0] as Boolean,
            hasContactsPermission = values[1] as Boolean,
            isLoading = values[2] as Boolean,
            activeTab = values[3] as ShareTab,
            searchQuery = values[4] as String,
            manualNumberInput = values[5] as String,
            callLogs = values[6] as List<CallLogEntry>,
            contacts = values[7] as List<ContactEntry>,
            cardProfile = values[8] as CardProfile,
            shareSettings = values[9] as ShareSettings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(
            hasCallLogPermission = _hasCallLogPerm.value,
            hasContactsPermission = _hasContactsPerm.value,
        ),
    )

    init {
        refreshDeviceRecords()
    }

    fun onPermissionsUpdated() {
        _hasCallLogPerm.value = phoneRepo.hasCallLogPermission()
        _hasContactsPerm.value = phoneRepo.hasContactsPermission()
        refreshDeviceRecords()
    }

    fun refreshDeviceRecords() {
        _hasCallLogPerm.value = phoneRepo.hasCallLogPermission()
        _hasContactsPerm.value = phoneRepo.hasContactsPermission()
        viewModelScope.launch {
            _isLoading.value = true
            val logs = phoneRepo.loadRecentCallLogs()
            val contacts = phoneRepo.loadContacts()
            _callLogs.value = logs
            _contacts.value = contacts
            _isLoading.value = false
        }
    }

    fun selectTab(tab: ShareTab) {
        _activeTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateManualNumberInput(input: String) {
        _manualNumberInput.value = input
    }

    fun sendWhatsAppTo(rawNumber: String, recipientName: String?) {
        val state = uiState.value
        val result = IntentLauncher.sendCardViaWhatsApp(
            context = getApplication(),
            rawNumber = rawNumber,
            defaultCountryCode = state.shareSettings.defaultCountryCode,
            greetingTemplate = state.shareSettings.greetingTemplate,
            cardShareUrl = state.activeCompany.businessCardUrl,
            recipientName = recipientName,
            preference = state.shareSettings.preferredWhatsAppApp,
            companyName = state.activeCompany.name,
        )

        when (result) {
            WhatsAppLaunchResult.Launched -> Unit
            WhatsAppLaunchResult.InvalidNumber -> {
                emitMessage("Please enter a valid phone number.")
            }
            WhatsAppLaunchResult.NotInstalled -> {
                emitMessage("WhatsApp is not installed on this device.")
            }
        }
    }

    fun sendWhatsAppToManualNumber() {
        val raw = _manualNumberInput.value
        sendWhatsAppTo(rawNumber = raw, recipientName = null)
    }

    fun formatPreviewNumber(rawNumber: String): String {
        val cc = uiState.value.shareSettings.defaultCountryCode
        return PhoneFormatter.formatDisplayWhatsApp(rawNumber, cc) ?: rawNumber
    }

    fun updateDefaultCountryCode(rawCode: String) {
        viewModelScope.launch {
            prefsRepo.updateDefaultCountryCode(rawCode)
            emitMessage("Default country code updated to ${PhoneFormatter.normalizeCountryCode(rawCode)}")
        }
    }

    fun updateGreetingAndCardLink(greeting: String, cardShareUrl: String) {
        viewModelScope.launch {
            prefsRepo.updateGreetingAndActiveCompanyCardUrl(greeting, cardShareUrl)
            emitMessage("Greeting & business card link updated")
        }
    }

    fun updatePreferredWhatsAppApp(pref: WhatsAppAppPreference) {
        viewModelScope.launch {
            prefsRepo.updatePreferredWhatsAppApp(pref)
        }
    }

    fun addCompany(
        name: String,
        businessCardUrl: String,
        websiteUrl: String,
        themeColorHex: Long,
        setAsActive: Boolean,
    ) {
        viewModelScope.launch {
            prefsRepo.addCompany(
                name = name,
                businessCardUrl = businessCardUrl,
                websiteUrl = websiteUrl,
                themeColorHex = themeColorHex,
                setAsActive = setAsActive,
            )
            emitMessage(
                if (setAsActive) "${name.trim()} added & activated" else "${name.trim()} added"
            )
        }
    }

    fun updateCompany(item: CompanyItem, setAsActive: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateCompany(item, setAsActive)
            emitMessage("${item.name.trim()} updated")
        }
    }

    fun deleteCompany(id: String) {
        viewModelScope.launch {
            prefsRepo.deleteCompany(id)
            emitMessage("Company removed")
        }
    }

    fun selectActiveCompany(id: String) {
        viewModelScope.launch {
            prefsRepo.setActiveCompany(id)
            val name = uiState.value.shareSettings.savedCompanies.firstOrNull { it.id == id }?.name
            if (name != null) {
                emitMessage("Switched to $name")
            }
        }
    }

    fun saveCardProfile(profile: CardProfile) {
        viewModelScope.launch {
            prefsRepo.saveCardProfile(profile)
            emitMessage("Digital business card saved")
        }
    }

    fun resetCardToDefaults() {
        viewModelScope.launch {
            prefsRepo.resetCardToDefaults()
            emitMessage("Business card restored to defaults")
        }
    }

    fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowMessage(message))
        }
    }
}
