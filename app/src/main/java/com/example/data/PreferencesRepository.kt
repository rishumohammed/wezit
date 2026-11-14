package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppThemePreset
import com.example.model.CardProfile
import com.example.model.CompanyItem
import com.example.model.ShareSettings
import com.example.model.WhatsAppAppPreference
import com.example.util.PhoneFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "samad_card_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val FULL_NAME = stringPreferencesKey("card_full_name")
        val INITIALS = stringPreferencesKey("card_initials")
        val TITLE = stringPreferencesKey("card_title")
        val ORGANIZATION = stringPreferencesKey("card_organization")
        val TAGLINE = stringPreferencesKey("card_tagline")
        val PHONE = stringPreferencesKey("card_phone")
        val WHATSAPP_PHONE = stringPreferencesKey("card_whatsapp_phone")
        val EMAIL = stringPreferencesKey("card_email")
        val WEBSITE_URL = stringPreferencesKey("card_website_url")
        val CARD_SHARE_URL = stringPreferencesKey("card_share_url")
        val ADDRESS = stringPreferencesKey("card_address")
        val SECONDARY_ADDRESS = stringPreferencesKey("card_secondary_address")
        val LOCATION_MAP_URL = stringPreferencesKey("card_location_map_url")
        val INSTAGRAM_URL = stringPreferencesKey("card_instagram_url")
        val FACEBOOK_URL = stringPreferencesKey("card_facebook_url")
        val YOUTUBE_URL = stringPreferencesKey("card_youtube_url")
        val BIO = stringPreferencesKey("card_bio")

        val DEFAULT_COUNTRY_CODE = stringPreferencesKey("share_default_country_code")
        val GREETING_TEMPLATE = stringPreferencesKey("share_greeting_template")
        val PREFERRED_WHATSAPP_APP = stringPreferencesKey("share_preferred_whatsapp_app")
        val SAVED_COMPANIES_JSON = stringPreferencesKey("share_saved_companies_json")
        val ACTIVE_COMPANY_ID = stringPreferencesKey("share_active_company_id")
    }

    val cardProfileFlow: Flow<CardProfile> = context.dataStore.data.map { prefs ->
        CardProfile(
            fullName = prefs[Keys.FULL_NAME] ?: CardProfile.DEFAULT_FULL_NAME,
            initials = prefs[Keys.INITIALS] ?: CardProfile.DEFAULT_INITIALS,
            title = prefs[Keys.TITLE] ?: CardProfile.DEFAULT_TITLE,
            organization = prefs[Keys.ORGANIZATION]
                ?.takeUnless { it == CardProfile.LEGACY_DEFAULT_ORGANIZATION }
                ?: CardProfile.DEFAULT_ORGANIZATION,
            tagline = prefs[Keys.TAGLINE] ?: CardProfile.DEFAULT_TAGLINE,
            phone = prefs[Keys.PHONE] ?: CardProfile.DEFAULT_PHONE,
            whatsappPhone = prefs[Keys.WHATSAPP_PHONE] ?: CardProfile.DEFAULT_WHATSAPP_PHONE,
            email = prefs[Keys.EMAIL] ?: CardProfile.DEFAULT_EMAIL,
            websiteUrl = prefs[Keys.WEBSITE_URL] ?: CardProfile.DEFAULT_WEBSITE_URL,
            cardShareUrl = prefs[Keys.CARD_SHARE_URL] ?: CardProfile.DEFAULT_CARD_SHARE_URL,
            address = prefs[Keys.ADDRESS] ?: CardProfile.DEFAULT_ADDRESS,
            secondaryAddress = prefs[Keys.SECONDARY_ADDRESS]
                ?: CardProfile.DEFAULT_SECONDARY_ADDRESS,
            locationMapUrl = prefs[Keys.LOCATION_MAP_URL] ?: CardProfile.DEFAULT_LOCATION_MAP_URL,
            instagramUrl = prefs[Keys.INSTAGRAM_URL] ?: CardProfile.DEFAULT_INSTAGRAM_URL,
            facebookUrl = prefs[Keys.FACEBOOK_URL] ?: CardProfile.DEFAULT_FACEBOOK_URL,
            youtubeUrl = prefs[Keys.YOUTUBE_URL] ?: CardProfile.DEFAULT_YOUTUBE_URL,
            bio = prefs[Keys.BIO] ?: CardProfile.DEFAULT_BIO,
        )
    }

    val shareSettingsFlow: Flow<ShareSettings> = context.dataStore.data.map { prefs ->
        val prefName = prefs[Keys.PREFERRED_WHATSAPP_APP]
        val appPref = WhatsAppAppPreference.entries.firstOrNull { it.name == prefName }
            ?: WhatsAppAppPreference.AUTO
        val savedCompanies = decodeCompanies(prefs[Keys.SAVED_COMPANIES_JSON])
        val activeId = prefs[Keys.ACTIVE_COMPANY_ID] ?: savedCompanies.first().id

        ShareSettings(
            defaultCountryCode = prefs[Keys.DEFAULT_COUNTRY_CODE]
                ?: ShareSettings.DEFAULT_COUNTRY_CODE,
            greetingTemplate = prefs[Keys.GREETING_TEMPLATE]
                ?.takeUnless { it == ShareSettings.LEGACY_DEFAULT_GREETING_TEMPLATE }
                ?: ShareSettings.DEFAULT_GREETING_TEMPLATE,
            preferredWhatsAppApp = appPref,
            savedCompanies = savedCompanies,
            activeCompanyId = activeId,
        )
    }

    suspend fun saveCardProfile(profile: CardProfile) {
        val cleanShareUrl = profile.cardShareUrl.trim().ifEmpty { CardProfile.DEFAULT_CARD_SHARE_URL }
        context.dataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = profile.fullName.trim().ifEmpty { CardProfile.DEFAULT_FULL_NAME }
            prefs[Keys.INITIALS] = profile.initials.trim().ifEmpty {
                deriveInitials(profile.fullName)
            }
            prefs[Keys.TITLE] = profile.title.trim()
            prefs[Keys.ORGANIZATION] = profile.organization.trim()
            prefs[Keys.TAGLINE] = profile.tagline.trim()
            prefs[Keys.PHONE] = profile.phone.trim()
            prefs[Keys.WHATSAPP_PHONE] = profile.whatsappPhone.trim()
            prefs[Keys.EMAIL] = profile.email.trim()
            prefs[Keys.WEBSITE_URL] = profile.websiteUrl.trim()
            prefs[Keys.CARD_SHARE_URL] = cleanShareUrl
            prefs[Keys.ADDRESS] = profile.address.trim()
            prefs[Keys.SECONDARY_ADDRESS] = profile.secondaryAddress.trim()
            prefs[Keys.LOCATION_MAP_URL] = profile.locationMapUrl.trim()
            prefs[Keys.INSTAGRAM_URL] = profile.instagramUrl.trim()
            prefs[Keys.FACEBOOK_URL] = profile.facebookUrl.trim()
            prefs[Keys.YOUTUBE_URL] = profile.youtubeUrl.trim()
            prefs[Keys.BIO] = profile.bio.trim()
        }
    }

    suspend fun updateDefaultCountryCode(rawCode: String) {
        val normalized = PhoneFormatter.normalizeCountryCode(rawCode)
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_COUNTRY_CODE] = normalized
        }
    }

    suspend fun updateGreetingTemplate(template: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GREETING_TEMPLATE] = template.trim()
        }
    }

    suspend fun updateGreetingAndActiveCompanyCardUrl(greeting: String, cardShareUrl: String) {
        val cleanUrl = normalizeUrl(cardShareUrl)
        context.dataStore.edit { prefs ->
            prefs[Keys.GREETING_TEMPLATE] = greeting.trim()
            if (cleanUrl.isNotEmpty()) {
                prefs[Keys.CARD_SHARE_URL] = cleanUrl
                val currentCompanies = decodeCompanies(prefs[Keys.SAVED_COMPANIES_JSON]).toMutableList()
                val activeId = prefs[Keys.ACTIVE_COMPANY_ID] ?: currentCompanies.first().id
                val idx = currentCompanies.indexOfFirst { it.id == activeId }
                if (idx >= 0) {
                    currentCompanies[idx] = currentCompanies[idx].copy(businessCardUrl = cleanUrl)
                    prefs[Keys.SAVED_COMPANIES_JSON] = encodeCompanies(currentCompanies)
                }
            }
        }
    }

    suspend fun updatePreferredWhatsAppApp(pref: WhatsAppAppPreference) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PREFERRED_WHATSAPP_APP] = pref.name
        }
    }

    suspend fun addCompany(
        name: String,
        businessCardUrl: String,
        websiteUrl: String,
        themeColorHex: Long,
        setAsActive: Boolean = true,
    ) {
        val cleanName = name.trim()
        val cleanCardUrl = normalizeUrl(businessCardUrl)
        val cleanWebsiteUrl = normalizeUrl(websiteUrl)
        if (cleanName.isEmpty() || (cleanCardUrl.isEmpty() && cleanWebsiteUrl.isEmpty())) return

        val resolvedCardUrl = cleanCardUrl.ifEmpty { cleanWebsiteUrl }
        val resolvedWebsiteUrl = cleanWebsiteUrl.ifEmpty { cleanCardUrl }
        val newCompany = CompanyItem(
            id = "company_${UUID.randomUUID().toString().take(8)}",
            name = cleanName,
            businessCardUrl = resolvedCardUrl,
            websiteUrl = resolvedWebsiteUrl,
            themeColorHex = themeColorHex,
        )

        context.dataStore.edit { prefs ->
            val current = decodeCompanies(prefs[Keys.SAVED_COMPANIES_JSON]).toMutableList()
            current.add(newCompany)
            prefs[Keys.SAVED_COMPANIES_JSON] = encodeCompanies(current)
            if (setAsActive) {
                prefs[Keys.ACTIVE_COMPANY_ID] = newCompany.id
                prefs[Keys.CARD_SHARE_URL] = newCompany.businessCardUrl
                prefs[Keys.WEBSITE_URL] = newCompany.websiteUrl
            }
        }
    }

    suspend fun updateCompany(updated: CompanyItem, setAsActive: Boolean = false) {
        val cleanName = updated.name.trim()
        val cleanCardUrl = normalizeUrl(updated.businessCardUrl)
        val cleanWebsiteUrl = normalizeUrl(updated.websiteUrl)
        if (cleanName.isEmpty() || (cleanCardUrl.isEmpty() && cleanWebsiteUrl.isEmpty())) return

        val saved = updated.copy(
            name = cleanName,
            businessCardUrl = cleanCardUrl.ifEmpty { cleanWebsiteUrl },
            websiteUrl = cleanWebsiteUrl.ifEmpty { cleanCardUrl },
        )

        context.dataStore.edit { prefs ->
            val current = decodeCompanies(prefs[Keys.SAVED_COMPANIES_JSON]).toMutableList()
            val idx = current.indexOfFirst { it.id == saved.id }
            if (idx >= 0) {
                current[idx] = saved
                prefs[Keys.SAVED_COMPANIES_JSON] = encodeCompanies(current)
                val activeId = prefs[Keys.ACTIVE_COMPANY_ID] ?: current.first().id
                if (setAsActive || activeId == saved.id) {
                    prefs[Keys.ACTIVE_COMPANY_ID] = saved.id
                    prefs[Keys.CARD_SHARE_URL] = saved.businessCardUrl
                    prefs[Keys.WEBSITE_URL] = saved.websiteUrl
                }
            }
        }
    }

    suspend fun deleteCompany(id: String) {
        context.dataStore.edit { prefs ->
            val current = decodeCompanies(prefs[Keys.SAVED_COMPANIES_JSON]).toMutableList()
            if (current.size <= 1) return@edit
            current.removeAll { it.id == id }
            prefs[Keys.SAVED_COMPANIES_JSON] = encodeCompanies(current)
            val activeId = prefs[Keys.ACTIVE_COMPANY_ID]
            if (activeId == id || current.none { it.id == activeId }) {
                val fallback = current.first()
                prefs[Keys.ACTIVE_COMPANY_ID] = fallback.id
                prefs[Keys.CARD_SHARE_URL] = fallback.businessCardUrl
                prefs[Keys.WEBSITE_URL] = fallback.websiteUrl
            }
        }
    }

    suspend fun setActiveCompany(id: String) {
        val currentSettings = shareSettingsFlow.first()
        val match = currentSettings.savedCompanies.firstOrNull { it.id == id } ?: return
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_COMPANY_ID] = match.id
            prefs[Keys.CARD_SHARE_URL] = match.businessCardUrl
            prefs[Keys.WEBSITE_URL] = match.websiteUrl
        }
    }

    suspend fun resetCardToDefaults() {
        saveCardProfile(CardProfile())
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun encodeCompanies(companies: List<CompanyItem>): String {
        val array = JSONArray()
        companies.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("businessCardUrl", item.businessCardUrl)
                    put("websiteUrl", item.websiteUrl)
                    put("themeColorHex", item.themeColorHex)
                }
            )
        }
        return array.toString()
    }

    private fun decodeCompanies(json: String?): List<CompanyItem> {
        if (json.isNullOrBlank()) return ShareSettings.DEFAULT_COMPANIES
        return runCatching {
            val array = JSONArray(json)
            val parsed = buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id").trim()
                    val name = obj.optString("name").trim()
                    val cardUrl = obj.optString("businessCardUrl").trim()
                    val websiteUrl = obj.optString("websiteUrl").trim()
                    val themeColorHex = obj.optLong(
                        "themeColorHex",
                        AppThemePreset.BOTANICAL_GREEN.primaryHex,
                    )
                    if (id.isNotEmpty() && name.isNotEmpty() && (cardUrl.isNotEmpty() || websiteUrl.isNotEmpty())) {
                        add(
                            CompanyItem(
                                id = id,
                                name = name,
                                businessCardUrl = cardUrl.ifEmpty { websiteUrl },
                                websiteUrl = websiteUrl.ifEmpty { cardUrl },
                                themeColorHex = themeColorHex,
                            )
                        )
                    }
                }
            }
            parsed.ifEmpty { ShareSettings.DEFAULT_COMPANIES }
        }.getOrElse { ShareSettings.DEFAULT_COMPANIES }
    }

    private fun deriveInitials(name: String): String {
        val tokens = name
            .replace("Dr.", "", ignoreCase = true)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        return when {
            tokens.size >= 2 -> "${tokens.first().first()}${tokens.last().first()}".uppercase()
            tokens.size == 1 -> tokens.first().take(2).uppercase()
            else -> CardProfile.DEFAULT_INITIALS
        }
    }
}
