package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.CompanyItem
import com.example.ui.MainViewModel
import com.example.ui.UiEvent
import com.example.ui.components.CardEditorDialog
import com.example.ui.components.CompanyEditorDialog
import com.example.ui.components.CountryCodeDialog
import com.example.ui.components.GreetingEditorDialog
import com.example.ui.components.QrCodeDialog
import com.example.ui.screens.MyCardScreen
import com.example.ui.screens.QuickShareScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SamadCardTheme
import com.example.util.IntentLauncher

const val ROUTE_QUICK_SHARE = "quick_share"
const val ROUTE_MY_CARD = "my_card"
const val ROUTE_SETTINGS = "settings"

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val TopDestinations = listOf(
    NavDestinationItem(
        route = ROUTE_QUICK_SHARE,
        labelRes = R.string.nav_quick_share,
        selectedIcon = Icons.AutoMirrored.Filled.Send,
        unselectedIcon = Icons.AutoMirrored.Outlined.Send,
        testTag = "nav_quick_share",
    ),
    NavDestinationItem(
        route = ROUTE_MY_CARD,
        labelRes = R.string.nav_my_card,
        selectedIcon = Icons.Filled.Badge,
        unselectedIcon = Icons.Outlined.Badge,
        testTag = "nav_my_card",
    ),
    NavDestinationItem(
        route = ROUTE_SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings",
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            SamadCardApp()
        }
    }
}

@Composable
fun SamadCardApp(
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCountryCodeDialog by remember { mutableStateOf(false) }
    var showGreetingDialog by remember { mutableStateOf(false) }
    var showCardEditorDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var showCompanyEditor by remember { mutableStateOf(false) }
    var editingCompany by remember { mutableStateOf<CompanyItem?>(null) }

    val copyToClipboard: (String, String) -> Unit = { label, value ->
        val clipboard = context.getSystemService(
            Context.CLIPBOARD_SERVICE
        ) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        viewModel.emitMessage("$label copied")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onPermissionsUpdated()
    }

    // Automatically refresh Call Logs & Contacts when returning from a phone call or settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onPermissionsUpdated()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Prompt once on initial launch if Call Log or Contacts permission is not granted yet
    LaunchedEffect(Unit) {
        if (!uiState.hasCallLogPermission || !uiState.hasContactsPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                )
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val activeThemeColor = ComposeColor(uiState.activeCompany.themeColorHex)
    val effectiveProfile = uiState.effectiveCardProfile

    SamadCardTheme(accentColor = activeThemeColor) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ) {
                            TopDestinations.forEach { dest ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == dest.route
                                } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(dest.route) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) {
                                                dest.selectedIcon
                                            } else {
                                                dest.unselectedIcon
                                            },
                                            contentDescription = stringResource(dest.labelRes),
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    modifier = Modifier.testTag(dest.testTag),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ) {
                            TopDestinations.forEach { dest ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == dest.route
                                } == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(dest.route) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) {
                                                dest.selectedIcon
                                            } else {
                                                dest.unselectedIcon
                                            },
                                            contentDescription = stringResource(dest.labelRes),
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    modifier = Modifier.testTag(dest.testTag),
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_QUICK_SHARE,
                            modifier = Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxSize(),
                        ) {
                            composable(ROUTE_QUICK_SHARE) {
                                QuickShareScreen(
                                    uiState = uiState,
                                    onRefresh = viewModel::refreshDeviceRecords,
                                    onRequestPermissions = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_CALL_LOG,
                                                Manifest.permission.READ_CONTACTS,
                                            )
                                        )
                                    },
                                    onOpenAppSettings = {
                                        IntentLauncher.openAppSettings(context)
                                    },
                                    onSelectTab = viewModel::selectTab,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onManualNumberChange = viewModel::updateManualNumberInput,
                                    onSendManualNumber = viewModel::sendWhatsAppToManualNumber,
                                    onSendWhatsAppTo = viewModel::sendWhatsAppTo,
                                    formatPreviewNumber = viewModel::formatPreviewNumber,
                                )
                            }

                            composable(ROUTE_MY_CARD) {
                                MyCardScreen(
                                    profile = effectiveProfile,
                                    defaultCountryCode = uiState.shareSettings.defaultCountryCode,
                                    activeCompany = uiState.activeCompany,
                                    onSaveContactClick = {
                                        IntentLauncher.saveContactToPhone(
                                            context = context,
                                            profile = effectiveProfile,
                                            defaultCountryCode = uiState.shareSettings.defaultCountryCode,
                                        )
                                    },
                                    onCallClick = { phone ->
                                        IntentLauncher.dialNumber(context, phone)
                                    },
                                    onWhatsAppSelfClick = { phone ->
                                        viewModel.sendWhatsAppTo(phone, effectiveProfile.fullName)
                                    },
                                    onWebsiteClick = { url ->
                                        IntentLauncher.openUrl(context, url)
                                    },
                                    onShareCardClick = {
                                        IntentLauncher.shareCard(
                                            context = context,
                                            profile = effectiveProfile,
                                            greetingTemplate = uiState.shareSettings.greetingTemplate,
                                            shareUrl = uiState.activeCompany.businessCardUrl,
                                            companyName = uiState.activeCompany.name,
                                        )
                                    },
                                    onQrCodeClick = { showQrCodeDialog = true },
                                    onCopyText = copyToClipboard,
                                    onEditCardClick = { showCardEditorDialog = true },
                                )
                            }

                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    cardProfile = uiState.cardProfile,
                                    shareSettings = uiState.shareSettings,
                                    onAddCompany = {
                                        editingCompany = null
                                        showCompanyEditor = true
                                    },
                                    onEditCompany = { company ->
                                        editingCompany = company
                                        showCompanyEditor = true
                                    },
                                    onDeleteCompany = viewModel::deleteCompany,
                                    onSelectActiveCompany = viewModel::selectActiveCompany,
                                    onOpenCountryCodeDialog = { showCountryCodeDialog = true },
                                    onOpenGreetingDialog = { showGreetingDialog = true },
                                    onSelectWhatsAppPreference = viewModel::updatePreferredWhatsAppApp,
                                    onOpenCardEditor = { showCardEditorDialog = true },
                                    onResetCardDefaults = viewModel::resetCardToDefaults,
                                )
                            }
                        }
                    }
                }
            }

            if (showCountryCodeDialog) {
                CountryCodeDialog(
                    currentCode = uiState.shareSettings.defaultCountryCode,
                    onDismiss = { showCountryCodeDialog = false },
                    onConfirm = viewModel::updateDefaultCountryCode,
                )
            }

            if (showGreetingDialog) {
                GreetingEditorDialog(
                    currentGreeting = uiState.shareSettings.greetingTemplate,
                    currentCardUrl = uiState.activeCompany.businessCardUrl,
                    activeCompanyName = uiState.activeCompany.name,
                    onDismiss = { showGreetingDialog = false },
                    onSave = viewModel::updateGreetingAndCardLink,
                )
            }

            if (showCardEditorDialog) {
                CardEditorDialog(
                    profile = uiState.cardProfile,
                    onDismiss = { showCardEditorDialog = false },
                    onSave = viewModel::saveCardProfile,
                )
            }

            if (showQrCodeDialog) {
                QrCodeDialog(
                    profile = effectiveProfile,
                    activeCompany = uiState.activeCompany,
                    onDismiss = { showQrCodeDialog = false },
                    onCopyLink = {
                        copyToClipboard(
                            uiState.activeCompany.name,
                            uiState.activeCompany.businessCardUrl,
                        )
                    },
                    onShare = {
                        IntentLauncher.shareCard(
                            context = context,
                            profile = effectiveProfile,
                            greetingTemplate = uiState.shareSettings.greetingTemplate,
                            shareUrl = uiState.activeCompany.businessCardUrl,
                            companyName = uiState.activeCompany.name,
                        )
                    },
                )
            }

            if (showCompanyEditor) {
                CompanyEditorDialog(
                    existing = editingCompany,
                    onDismiss = { showCompanyEditor = false },
                    onSave = { name, cardUrl, websiteUrl, themeHex, setAsActive ->
                        val target = editingCompany
                        if (target == null) {
                            viewModel.addCompany(
                                name = name,
                                businessCardUrl = cardUrl,
                                websiteUrl = websiteUrl,
                                themeColorHex = themeHex,
                                setAsActive = setAsActive,
                            )
                        } else {
                            viewModel.updateCompany(
                                target.copy(
                                    name = name,
                                    businessCardUrl = cardUrl,
                                    websiteUrl = websiteUrl,
                                    themeColorHex = themeHex,
                                ),
                                setAsActive = setAsActive,
                            )
                        }
                    },
                )
            }
        }
    }
}
