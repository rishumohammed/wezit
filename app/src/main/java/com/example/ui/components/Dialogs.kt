package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppThemePreset
import com.example.model.CardProfile
import com.example.model.CompanyItem
import com.example.util.PhoneFormatter
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

private val CommonCountryCodes = listOf(
    "+91" to "India (+91)",
    "+971" to "UAE (+971)",
    "+966" to "KSA (+966)",
    "+974" to "Qatar (+974)",
    "+965" to "Kuwait (+965)",
    "+968" to "Oman (+968)",
    "+1" to "US/CA (+1)",
    "+44" to "UK (+44)",
    "+65" to "SG (+65)",
    "+60" to "MY (+60)",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanyEditorDialog(
    existing: CompanyItem?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        businessCardUrl: String,
        websiteUrl: String,
        themeColorHex: Long,
        setAsActive: Boolean,
    ) -> Unit,
) {
    val initialHex = existing?.themeColorHex ?: AppThemePreset.BOTANICAL_GREEN.primaryHex
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var businessCardUrl by remember(existing) { mutableStateOf(existing?.businessCardUrl.orEmpty()) }
    var websiteUrl by remember(existing) { mutableStateOf(existing?.websiteUrl.orEmpty()) }
    var selectedColorHex by remember(existing) { mutableLongStateOf(initialHex) }
    var hexInput by remember(existing) {
        mutableStateOf(AppThemePreset.formatHexColor(initialHex))
    }
    var setAsActive by remember(existing) { mutableStateOf(true) }

    val activePreset = AppThemePreset.findMatchingPreset(selectedColorHex)
    val previewColor = Color(selectedColorHex)
    val canSave = name.trim().isNotEmpty() &&
        (businessCardUrl.trim().isNotEmpty() || websiteUrl.trim().isNotEmpty())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = if (existing == null) "Add Company" else "Edit Company",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Add a company's business card link, website, and website color theme.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("company_name_input"),
                )

                OutlinedTextField(
                    value = businessCardUrl,
                    onValueChange = { businessCardUrl = it },
                    label = { Text("Business Card Link (https://...)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("company_card_url_input"),
                )

                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Website Link (https://...)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("company_website_url_input"),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Website Theme",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = CircleShape,
                            color = previewColor,
                        ) {
                            Text(
                                text = activePreset?.displayName ?: AppThemePreset.formatHexColor(selectedColorHex),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (previewColor.luminance() > 0.50f) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AppThemePreset.entries.forEach { preset ->
                            val isSelected =
                                (selectedColorHex and 0x00FFFFFFL) == (preset.primaryHex and 0x00FFFFFFL)
                            val swatchColor = Color(preset.primaryHex)
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedColorHex = preset.primaryHex
                                        hexInput = AppThemePreset.formatHexColor(preset.primaryHex)
                                    }
                                    .testTag("theme_swatch_${preset.id}"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.5.dp, Color.White, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "${preset.displayName} selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { raw ->
                            val filtered = raw.filter {
                                it == '#' || it.isDigit() || it in 'a'..'f' || it in 'A'..'F'
                            }.take(7)
                            hexInput = filtered
                            AppThemePreset.parseHexColor(filtered)?.let { parsed ->
                                selectedColorHex = parsed
                            }
                        },
                        label = { Text("Custom Theme Hex (e.g. #0E7A1D)") },
                        singleLine = true,
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(previewColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("company_theme_hex_input"),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { setAsActive = !setAsActive },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = setAsActive,
                        onCheckedChange = { setAsActive = it },
                    )
                    Text(
                        text = "Switch to this company after saving",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        onSave(name, businessCardUrl, websiteUrl, selectedColorHex, setAsActive)
                        onDismiss()
                    }
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = previewColor,
                    contentColor = if (previewColor.luminance() > 0.50f) Color.Black else Color.White,
                ),
                modifier = Modifier.testTag("save_company_button"),
            ) {
                Text("Save Company")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun QrCodeDialog(
    profile: CardProfile,
    activeCompany: CompanyItem,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
) {
    val qrPayload = activeCompany.businessCardUrl.ifBlank { activeCompany.websiteUrl }
    val matrix = remember(qrPayload) {
        runCatching {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            QRCodeWriter().encode(qrPayload, BarcodeFormat.QR_CODE, 64, 64, hints)
        }.getOrNull()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = activeCompany.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.padding(4.dp),
                ) {
                    val moduleColor = MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .background(Color.White)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (matrix != null) {
                            Canvas(modifier = Modifier.size(182.dp)) {
                                val cellW = size.width / matrix.width
                                val cellH = size.height / matrix.height
                                for (y in 0 until matrix.height) {
                                    for (x in 0 until matrix.width) {
                                        if (matrix.get(x, y)) {
                                            drawRect(
                                                color = moduleColor,
                                                topLeft = Offset(x * cellW, y * cellH),
                                                size = Size(cellW + 0.5f, cellH + 0.5f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onCopyLink,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Copy Link")
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Share")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountryCodeDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var codeText by remember(currentCode) { mutableStateOf(currentCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = "Default Country Code",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Local numbers without a '+' prefix will use this code when opening WhatsApp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = codeText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() || it == '+' }.take(5)
                        codeText = cleaned
                    },
                    label = { Text("Country Code (e.g. +91)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("country_code_input"),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CommonCountryCodes.forEach { (code, label) ->
                        FilterChip(
                            selected = PhoneFormatter.normalizeCountryCode(codeText) == code,
                            onClick = { codeText = code },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(PhoneFormatter.normalizeCountryCode(codeText))
                    onDismiss()
                },
                modifier = Modifier.testTag("save_country_code_button"),
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun GreetingEditorDialog(
    currentGreeting: String,
    currentCardUrl: String,
    activeCompanyName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var greeting by remember(currentGreeting) { mutableStateOf(currentGreeting) }
    var cardUrl by remember(currentCardUrl) { mutableStateOf(currentCardUrl) }

    val previewText = remember(greeting, cardUrl, activeCompanyName) {
        PhoneFormatter.buildWhatsAppMessage(
            greetingTemplate = greeting,
            cardShareUrl = cardUrl,
            recipientName = "Dr. Ahmed",
            companyName = activeCompanyName,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = "Greeting & Card Link",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = greeting,
                    onValueChange = { greeting = it },
                    label = { Text("Custom Greeting Message") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("greeting_template_input"),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (!greeting.contains("{name}")) {
                                greeting = if (greeting.isBlank()) {
                                    "Greetings{name}, here is my digital business card:"
                                } else {
                                    "$greeting {name}"
                                }
                            }
                        },
                    ) {
                        Text("Insert {name}")
                    }
                    OutlinedButton(
                        onClick = {
                            if (!greeting.contains("{company}")) {
                                greeting = if (greeting.isBlank()) {
                                    "Greetings, thank you for connecting with {company}:"
                                } else {
                                    "$greeting {company}"
                                }
                            }
                        },
                    ) {
                        Text("Insert {company}")
                    }
                }

                OutlinedTextField(
                    value = cardUrl,
                    onValueChange = { cardUrl = it },
                    label = { Text("Active Business Card URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_link_input"),
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "LIVE WHATSAPP PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(greeting, cardUrl)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_greeting_button"),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun CardEditorDialog(
    profile: CardProfile,
    onDismiss: () -> Unit,
    onSave: (CardProfile) -> Unit,
) {
    var fullName by remember(profile) { mutableStateOf(profile.fullName) }
    var initials by remember(profile) { mutableStateOf(profile.initials) }
    var title by remember(profile) { mutableStateOf(profile.title) }
    var organization by remember(profile) { mutableStateOf(profile.organization) }
    var tagline by remember(profile) { mutableStateOf(profile.tagline) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var whatsappPhone by remember(profile) { mutableStateOf(profile.whatsappPhone) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var websiteUrl by remember(profile) { mutableStateOf(profile.websiteUrl) }
    var cardShareUrl by remember(profile) { mutableStateOf(profile.cardShareUrl) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var secondaryAddress by remember(profile) { mutableStateOf(profile.secondaryAddress) }
    var locationMapUrl by remember(profile) { mutableStateOf(profile.locationMapUrl) }
    var instagramUrl by remember(profile) { mutableStateOf(profile.instagramUrl) }
    var facebookUrl by remember(profile) { mutableStateOf(profile.facebookUrl) }
    var youtubeUrl by remember(profile) { mutableStateOf(profile.youtubeUrl) }
    var bio by remember(profile) { mutableStateOf(profile.bio) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Edit Digital Business Card",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Customize the profile details shown on your native card and shared text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = initials,
                        onValueChange = { initials = it.take(3).uppercase() },
                        label = { Text("Monogram") },
                        singleLine = true,
                        modifier = Modifier.weight(0.32f),
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Designation") },
                        singleLine = true,
                        modifier = Modifier.weight(0.68f),
                    )
                }
                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Organization / Institute") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Direct Phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = whatsappPhone,
                    onValueChange = { whatsappPhone = it },
                    label = { Text("WhatsApp Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Primary Center Address") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = secondaryAddress,
                    onValueChange = { secondaryAddress = it },
                    label = { Text("Secondary Center Address") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = locationMapUrl,
                    onValueChange = { locationMapUrl = it },
                    label = { Text("Google Maps Location URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Website URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = instagramUrl,
                    onValueChange = { instagramUrl = it },
                    label = { Text("Instagram URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = facebookUrl,
                    onValueChange = { facebookUrl = it },
                    label = { Text("Facebook URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = cardShareUrl,
                    onValueChange = { cardShareUrl = it },
                    label = { Text("Default Card URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("About / Bio") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(
                                CardProfile(
                                    fullName = fullName,
                                    initials = initials,
                                    title = title,
                                    organization = organization,
                                    tagline = tagline,
                                    phone = phone,
                                    whatsappPhone = whatsappPhone,
                                    email = email,
                                    websiteUrl = websiteUrl,
                                    cardShareUrl = cardShareUrl,
                                    address = address,
                                    secondaryAddress = secondaryAddress,
                                    locationMapUrl = locationMapUrl,
                                    instagramUrl = instagramUrl,
                                    facebookUrl = facebookUrl,
                                    youtubeUrl = youtubeUrl,
                                    bio = bio,
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_card_profile_button"),
                    ) {
                        Text("Save Card")
                    }
                }
            }
        }
    }
}
