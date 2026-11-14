package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CardProfile
import com.example.model.CompanyItem
import com.example.model.ShareSettings
import com.example.util.PhoneFormatter

@Composable
fun MyCardScreen(
    profile: CardProfile,
    defaultCountryCode: String,
    activeCompany: CompanyItem,
    onSaveContactClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onWhatsAppSelfClick: (String) -> Unit,
    onWebsiteClick: (String) -> Unit,
    onShareCardClick: () -> Unit,
    onQrCodeClick: () -> Unit,
    onCopyText: (label: String, value: String) -> Unit,
    onEditCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedPhone = PhoneFormatter.formatDisplayWhatsApp(profile.phone, defaultCountryCode)
        ?: profile.phone
    val formattedWhatsApp =
        PhoneFormatter.formatDisplayWhatsApp(profile.whatsappPhone, defaultCountryCode)
            ?: profile.whatsappPhone
    val cleanWebsiteDisplay = activeCompany.websiteUrl
        .ifBlank { profile.websiteUrl }
        .removePrefix("https://")
        .removePrefix("http://")
        .trimEnd('/')

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Main Clean Card Sheet matching reference layout & active company theme
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Theme Accent Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Top Row: NFC CARD pill + Edit + Share + QR Code pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contactless,
                                    contentDescription = "NFC Card",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "NFC CARD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onEditCardClick)
                                    .testTag("edit_card_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Card",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onShareCardClick)
                                    .testTag("share_card_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Card",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onQrCodeClick)
                                    .testTag("qr_code_button"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 9.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "QR Code",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    // Active Company Brand Header (Samad's Elements logo or themed company crest)
                    if (activeCompany.id == ShareSettings.DEFAULT_COMPANY_ID) {
                        Image(
                            painter = painterResource(id = R.drawable.img_samads_elements_logo),
                            contentDescription = "Samad's Elements Traditional Treatment Centre logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(74.dp)
                                .padding(horizontal = 16.dp),
                        )
                    } else {
                        ThemedCompanyCrest(companyName = activeCompany.name)
                    }

                    // Name, Uppercase Tracked Designation & Organization
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        if (profile.title.isNotBlank()) {
                            Text(
                                text = profile.title.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.6.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                        if (profile.organization.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile.organization,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    // Primary Full-Width Save Contact CTA
                    Button(
                        onClick = onSaveContactClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_contact_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Contact to Phone",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }

                    // 4 Quick Action Square-Rounded Cards: Call | WhatsApp | Location | Website
                    val waTarget = profile.whatsappPhone.ifBlank { profile.phone }
                    val siteTarget = activeCompany.websiteUrl.ifBlank { profile.websiteUrl }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        QuickActionSquareCard(
                            icon = Icons.Default.Call,
                            label = "Call",
                            onClick = { onCallClick(profile.phone) },
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionSquareCard(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            label = "WhatsApp",
                            onClick = { onWhatsAppSelfClick(waTarget) },
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionSquareCard(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            onClick = {
                                onWebsiteClick(
                                    profile.locationMapUrl.ifBlank { siteTarget }
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionSquareCard(
                            icon = Icons.Default.Language,
                            label = "Website",
                            onClick = { onWebsiteClick(siteTarget) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Grouped Contact Info Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (profile.phone.isNotBlank()) {
                                CardDirectoryRow(
                                    icon = Icons.Default.Call,
                                    label = "DIRECT PHONE",
                                    value = formattedPhone,
                                    trailingIcon = Icons.Default.ContentCopy,
                                    trailingDesc = "Copy direct phone number",
                                    onRowClick = { onCallClick(profile.phone) },
                                    onTrailingClick = { onCopyText("Phone number", formattedPhone) },
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }

                            if (profile.whatsappPhone.isNotBlank()) {
                                CardDirectoryRow(
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    label = "WHATSAPP CONSULTATION",
                                    value = formattedWhatsApp,
                                    trailingIcon = Icons.Default.ContentCopy,
                                    trailingDesc = "Copy WhatsApp number",
                                    onRowClick = { onWhatsAppSelfClick(profile.whatsappPhone) },
                                    onTrailingClick = {
                                        onCopyText("WhatsApp number", formattedWhatsApp)
                                    },
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }

                            if (siteTarget.isNotBlank()) {
                                CardDirectoryRow(
                                    icon = Icons.Default.Language,
                                    label = "OFFICIAL WEBSITE",
                                    value = cleanWebsiteDisplay,
                                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                                    trailingDesc = "Open official website",
                                    onRowClick = { onWebsiteClick(siteTarget) },
                                    onTrailingClick = { onWebsiteClick(siteTarget) },
                                )
                            }
                        }
                    }

                    // Treatment Centre Card with Google Maps Link & Both Kerala Branches
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "TREATMENT CENTRE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                    )
                                }

                                if (profile.locationMapUrl.isNotBlank()) {
                                    Text(
                                        text = "Google Maps ↗",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onWebsiteClick(profile.locationMapUrl) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                    )
                                }
                            }

                            if (profile.address.isNotBlank()) {
                                BranchAddressBox(
                                    branchTitle = "THIRUVANANTHAPURAM",
                                    address = profile.address,
                                    onCopyClick = {
                                        onCopyText("Thiruvananthapuram address", profile.address)
                                    },
                                    onOpenMapClick = {
                                        if (profile.locationMapUrl.isNotBlank()) {
                                            onWebsiteClick(profile.locationMapUrl)
                                        }
                                    },
                                )
                            }

                            if (profile.secondaryAddress.isNotBlank()) {
                                BranchAddressBox(
                                    branchTitle = "KOLLAM",
                                    address = profile.secondaryAddress,
                                    onCopyClick = {
                                        onCopyText("Kollam address", profile.secondaryAddress)
                                    },
                                    onOpenMapClick = {
                                        if (profile.locationMapUrl.isNotBlank()) {
                                            onWebsiteClick(profile.locationMapUrl)
                                        }
                                    },
                                )
                            }
                        }
                    }

                    // Connect Online Footer
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "CONNECT ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.6.sp,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (siteTarget.isNotBlank()) {
                                SocialCircleButton(
                                    icon = Icons.Default.Public,
                                    label = "Website",
                                    testTagName = "social_website_button",
                                    onClick = { onWebsiteClick(siteTarget) },
                                )
                            }
                            if (profile.instagramUrl.isNotBlank()) {
                                SocialCircleButton(
                                    icon = Icons.Default.CameraAlt,
                                    label = "Instagram",
                                    testTagName = "social_instagram_button",
                                    onClick = { onWebsiteClick(profile.instagramUrl) },
                                )
                            }
                            if (profile.facebookUrl.isNotBlank()) {
                                SocialCircleButton(
                                    icon = Icons.Default.Share,
                                    label = "Facebook",
                                    testTagName = "social_facebook_button",
                                    onClick = { onWebsiteClick(profile.facebookUrl) },
                                )
                            }
                            if (profile.youtubeUrl.isNotBlank()) {
                                SocialCircleButton(
                                    icon = Icons.Default.PlayArrow,
                                    label = "YouTube",
                                    testTagName = "social_youtube_button",
                                    onClick = { onWebsiteClick(profile.youtubeUrl) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun ThemedCompanyCrest(companyName: String) {
    val letters = companyName
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .let { words ->
            when {
                words.size >= 2 -> "${words.first().first()}${words[1].first()}".uppercase()
                words.size == 1 -> words.first().take(2).uppercase()
                else -> "WZ"
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letters,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = companyName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.4.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuickActionSquareCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CardDirectoryRow(
    icon: ImageVector,
    label: String,
    value: String,
    trailingIcon: ImageVector,
    trailingDesc: String,
    onRowClick: () -> Unit,
    onTrailingClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = onTrailingClick,
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingDesc,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun BranchAddressBox(
    branchTitle: String,
    address: String,
    onCopyClick: () -> Unit,
    onOpenMapClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenMapClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = branchTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onCopyClick,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $branchTitle address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SocialCircleButton(
    icon: ImageVector,
    label: String,
    testTagName: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTagName),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
