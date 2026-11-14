package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import com.example.model.CardProfile
import com.example.model.WhatsAppAppPreference

sealed interface WhatsAppLaunchResult {
    data object Launched : WhatsAppLaunchResult
    data object NotInstalled : WhatsAppLaunchResult
    data object InvalidNumber : WhatsAppLaunchResult
}

object IntentLauncher {

    private const val PKG_WHATSAPP = "com.whatsapp"
    private const val PKG_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    fun sendCardViaWhatsApp(
        context: Context,
        rawNumber: String,
        defaultCountryCode: String,
        greetingTemplate: String,
        cardShareUrl: String,
        recipientName: String?,
        preference: WhatsAppAppPreference,
        companyName: String? = "Samad's Elements",
    ): WhatsAppLaunchResult {
        val formattedDigits = PhoneFormatter.formatForWhatsApp(rawNumber, defaultCountryCode)
            ?: return WhatsAppLaunchResult.InvalidNumber

        val message = PhoneFormatter.buildWhatsAppMessage(
            greetingTemplate = greetingTemplate,
            cardShareUrl = cardShareUrl,
            recipientName = recipientName,
            companyName = companyName,
        )

        val uri = Uri.parse(
            "https://api.whatsapp.com/send?phone=$formattedDigits&text=${Uri.encode(message)}"
        )

        val targetPackage = resolveWhatsAppPackage(context, preference)
        if (targetPackage != null) {
            val targetedIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(targetPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(targetedIntent)
                return WhatsAppLaunchResult.Launched
            } catch (_: ActivityNotFoundException) {
                // Fall through to generic ACTION_VIEW below
            }
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(fallbackIntent)
            WhatsAppLaunchResult.Launched
        } catch (_: ActivityNotFoundException) {
            WhatsAppLaunchResult.NotInstalled
        }
    }

    private fun resolveWhatsAppPackage(
        context: Context,
        preference: WhatsAppAppPreference,
    ): String? {
        val hasStandard = isPackageInstalled(context, PKG_WHATSAPP)
        val hasBusiness = isPackageInstalled(context, PKG_WHATSAPP_BUSINESS)

        return when (preference) {
            WhatsAppAppPreference.WHATSAPP -> if (hasStandard) PKG_WHATSAPP else null
            WhatsAppAppPreference.WHATSAPP_BUSINESS ->
                if (hasBusiness) PKG_WHATSAPP_BUSINESS else null
            WhatsAppAppPreference.AUTO -> when {
                hasStandard -> PKG_WHATSAPP
                hasBusiness -> PKG_WHATSAPP_BUSINESS
                else -> null
            }
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun dialNumber(context: Context, phone: String): Boolean {
        val clean = phone.trim()
        if (clean.isEmpty()) return false
        return try {
            context.startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(clean)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun sendEmail(context: Context, email: String, subject: String): Boolean {
        val clean = email.trim()
        if (clean.isEmpty()) return false
        return try {
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$clean")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun openUrl(context: Context, rawUrl: String): Boolean {
        val clean = rawUrl.trim()
        if (clean.isEmpty()) return false
        val normalized = if (clean.startsWith("http://") || clean.startsWith("https://")) {
            clean
        } else {
            "https://$clean"
        }
        return try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun saveContactToPhone(
        context: Context,
        profile: CardProfile,
        defaultCountryCode: String,
    ): Boolean {
        val primaryPhone = PhoneFormatter.formatDisplayWhatsApp(profile.phone, defaultCountryCode)
            ?: profile.phone
        val secondaryPhone =
            PhoneFormatter.formatDisplayWhatsApp(profile.whatsappPhone, defaultCountryCode)
                ?: profile.whatsappPhone

        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, profile.fullName)
            putExtra(ContactsContract.Intents.Insert.COMPANY, profile.organization)
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, profile.title)
            if (primaryPhone.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.PHONE, primaryPhone)
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
                )
            }
            if (secondaryPhone.isNotBlank() && secondaryPhone != primaryPhone) {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, secondaryPhone)
                putExtra(
                    ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                )
            }
            if (profile.email.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.EMAIL, profile.email)
            }
            if (profile.address.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.POSTAL, profile.address)
            }
            val notes = listOfNotNull(
                profile.websiteUrl.takeIf { it.isNotBlank() }?.let { "Website: $it" },
                profile.locationMapUrl.takeIf { it.isNotBlank() }?.let { "Map: $it" },
                profile.cardShareUrl.takeIf { it.isNotBlank() }?.let { "Digital Card: $it" },
            ).joinToString("\n")
            if (notes.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.NOTES, notes)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun buildShareCardText(
        profile: CardProfile,
        greetingTemplate: String,
        shareUrl: String = profile.cardShareUrl,
        companyName: String? = "Samad's Elements",
    ): String = buildString {
        append(profile.fullName)
        if (profile.title.isNotBlank()) append("\n${profile.title}")
        if (profile.organization.isNotBlank()) append("\n${profile.organization}")
        if (profile.phone.isNotBlank()) append("\nPhone: ${profile.phone}")
        if (profile.whatsappPhone.isNotBlank()) append("\nWhatsApp: ${profile.whatsappPhone}")
        if (profile.websiteUrl.isNotBlank()) append("\nWebsite: ${profile.websiteUrl}")
        if (profile.locationMapUrl.isNotBlank()) append("\nLocation: ${profile.locationMapUrl}")
        append("\n\n")
        append(
            PhoneFormatter.buildWhatsAppMessage(
                greetingTemplate = greetingTemplate,
                cardShareUrl = shareUrl,
                recipientName = null,
                companyName = companyName,
            )
        )
    }

    fun shareCard(
        context: Context,
        profile: CardProfile,
        greetingTemplate: String,
        shareUrl: String = profile.cardShareUrl,
        companyName: String? = "Samad's Elements",
    ) {
        val text = buildShareCardText(
            profile = profile,
            greetingTemplate = greetingTemplate,
            shareUrl = shareUrl,
            companyName = companyName,
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${profile.fullName} — Digital Business Card")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Digital Business Card").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
