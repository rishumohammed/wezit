package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CallLogEntry
import com.example.model.CallType
import com.example.model.ContactEntry
import com.example.model.ShareTab
import com.example.ui.MainUiState

@Composable
fun QuickShareScreen(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onSelectTab: (ShareTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onManualNumberChange: (String) -> Unit,
    onSendManualNumber: () -> Unit,
    onSendWhatsAppTo: (rawNumber: String, recipientName: String?) -> Unit,
    formatPreviewNumber: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val missingAnyPermission = !uiState.hasCallLogPermission || !uiState.hasContactsPermission
    val activeNeedsPermission = when (uiState.activeTab) {
        ShareTab.RECENT_CALLS -> !uiState.hasCallLogPermission
        ShareTab.CONTACTS -> !uiState.hasContactsPermission
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Minimal Header directly on canvas: WEZIT + Active Company + Refresh
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = uiState.activeCompany.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRefresh)
                            .minimumInteractiveComponentSize()
                            .testTag("refresh_logs_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh_cd),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            // Compact Permission Card if either Call Log or Contacts permission is missing
            if (missingAnyPermission) {
                item {
                    MinimalPermissionsCard(
                        hasCallLog = uiState.hasCallLogPermission,
                        hasContacts = uiState.hasContactsPermission,
                        onGrantClick = onRequestPermissions,
                        onOpenSettingsClick = onOpenAppSettings,
                    )
                }
            }

            // Hero Last Called Card (1-Tap Send)
            val lastCalled = uiState.lastCalledEntry
            if (uiState.hasCallLogPermission && lastCalled != null) {
                item {
                    LastCalledHeroCard(
                        entry = lastCalled,
                        formattedNumber = formatPreviewNumber(lastCalled.phoneNumber),
                        onSendClick = {
                            onSendWhatsAppTo(lastCalled.phoneNumber, lastCalled.displayName)
                        },
                    )
                }
            }

            // Segmented Switcher: Recent Calls | Contacts
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SegmentedButton(
                        selected = uiState.activeTab == ShareTab.RECENT_CALLS,
                        onClick = { onSelectTab(ShareTab.RECENT_CALLS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.primary,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = SegmentedButtonDefaults.borderStroke(
                            color = MaterialTheme.colorScheme.outline,
                        ),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                        },
                        modifier = Modifier.testTag("tab_recent_calls"),
                    ) {
                        Text(stringResource(R.string.tab_recent_calls))
                    }

                    SegmentedButton(
                        selected = uiState.activeTab == ShareTab.CONTACTS,
                        onClick = { onSelectTab(ShareTab.CONTACTS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.primary,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = SegmentedButtonDefaults.borderStroke(
                            color = MaterialTheme.colorScheme.outline,
                        ),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                        },
                        modifier = Modifier.testTag("tab_contacts"),
                    ) {
                        Text(stringResource(R.string.tab_contacts))
                    }
                }
            }

            if (!activeNeedsPermission) {
                // Minimal Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_contacts_input"),
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.5.dp)
                        }
                    }
                } else if (uiState.activeTab == ShareTab.RECENT_CALLS) {
                    val calls = uiState.filteredCallLogs
                    if (calls.isEmpty()) {
                        item {
                            EmptyListCard(
                                title = stringResource(R.string.empty_calls_title),
                                description = stringResource(R.string.empty_calls_desc),
                            )
                        }
                    } else {
                        itemsIndexed(calls, key = { _, it -> "call_${it.id}" }) { index, entry ->
                            GroupedDirectoryItemSurface(
                                isFirst = index == 0,
                                isLast = index == calls.lastIndex,
                            ) {
                                CallLogRow(
                                    entry = entry,
                                    formattedNumber = formatPreviewNumber(entry.phoneNumber),
                                    onSendClick = {
                                        onSendWhatsAppTo(entry.phoneNumber, entry.displayName)
                                    },
                                )
                            }
                        }
                    }
                } else {
                    val contacts = uiState.filteredContacts
                    if (contacts.isEmpty()) {
                        item {
                            EmptyListCard(
                                title = stringResource(R.string.empty_contacts_title),
                                description = stringResource(R.string.empty_contacts_desc),
                            )
                        }
                    } else {
                        itemsIndexed(
                            contacts,
                            key = { _, it -> "contact_${it.id}_${it.phoneNumber}" },
                        ) { index, contact ->
                            GroupedDirectoryItemSurface(
                                isFirst = index == 0,
                                isLast = index == contacts.lastIndex,
                            ) {
                                ContactRow(
                                    contact = contact,
                                    formattedNumber = formatPreviewNumber(contact.phoneNumber),
                                    onSendClick = {
                                        onSendWhatsAppTo(contact.phoneNumber, contact.displayName)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Docked Custom Phone Number Send Box at Bottom
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    ManualQuickSendRow(
                        manualNumber = uiState.manualNumberInput,
                        countryCode = uiState.shareSettings.defaultCountryCode,
                        onNumberChange = onManualNumberChange,
                        onSendClick = onSendManualNumber,
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalPermissionsCard(
    hasCallLog: Boolean,
    hasContacts: Boolean,
    onGrantClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Call Logs & Contacts Access",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = buildString {
                    append("Allow access to share your card with ")
                    append(
                        when {
                            !hasCallLog && !hasContacts -> "recent callers and contacts"
                            !hasCallLog -> "recent callers"
                            else -> "saved contacts"
                        }
                    )
                    append(" in one tap.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("grant_permission_button"),
                ) {
                    Text("Allow Access")
                }
                TextButton(
                    onClick = onOpenSettingsClick,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_settings_button"),
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            }
        }
    }
}

@Composable
private fun LastCalledHeroCard(
    entry: CallLogEntry,
    formattedNumber: String,
    onSendClick: () -> Unit,
) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        entry.timestampMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.last_called_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = callTypeIcon(entry.callType),
                        contentDescription = entry.callType.name,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column {
                Text(
                    text = entry.displayName ?: entry.phoneNumber,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onSendClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_last_called_whatsapp_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.send_card_whatsapp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ManualQuickSendRow(
    manualNumber: String,
    countryCode: String,
    onNumberChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = manualNumber,
            onValueChange = onNumberChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.manual_number_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            prefix = {
                Text(
                    text = "$countryCode ",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSendClick() }),
            modifier = Modifier
                .weight(1f)
                .testTag("manual_number_input"),
        )

        Button(
            onClick = onSendClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(54.dp)
                .testTag("manual_send_whatsapp_button"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send digital card to entered number on WhatsApp",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun GroupedDirectoryItemSurface(
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable () -> Unit,
) {
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(18.dp)
        isFirst -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
        isLast -> RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
        else -> RoundedCornerShape(0.dp)
    }
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
            if (!isLast) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun CallLogRow(
    entry: CallLogEntry,
    formattedNumber: String,
    onSendClick: () -> Unit,
) {
    val title = entry.displayName ?: entry.phoneNumber
    val initials = deriveAvatarInitials(entry.displayName ?: entry.phoneNumber)
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        entry.timestampMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSendClick)
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
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = callTypeIcon(entry.callType),
                    contentDescription = entry.callType.name,
                    tint = if (entry.callType == CallType.MISSED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "$formattedNumber • $relativeTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .size(40.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable(onClick = onSendClick)
                .testTag("row_whatsapp_button_${entry.id}"),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send business card via WhatsApp to $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: ContactEntry,
    formattedNumber: String,
    onSendClick: () -> Unit,
) {
    val initials = deriveAvatarInitials(contact.displayName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSendClick)
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
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${contact.label} • $formattedNumber",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .size(40.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable(onClick = onSendClick)
                .testTag("contact_whatsapp_button_${contact.id}"),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send business card via WhatsApp to ${contact.displayName}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyListCard(title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun callTypeIcon(callType: CallType) = when (callType) {
    CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
    CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
    CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
    CallType.OTHER -> Icons.Default.Call
}

private fun deriveAvatarInitials(nameOrNumber: String): String {
    val letters = nameOrNumber
        .split(Regex("\\s+"))
        .filter { it.any { c -> c.isLetter() } }
    return when {
        letters.size >= 2 ->
            "${letters.first().first { it.isLetter() }}${letters.last().first { it.isLetter() }}".uppercase()
        letters.size == 1 -> letters.first().filter { it.isLetter() }.take(2).uppercase()
        else -> "#"
    }
}
