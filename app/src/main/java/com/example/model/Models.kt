package com.example.model

enum class AppThemePreset(
    val id: String,
    val displayName: String,
    val primaryHex: Long,
) {
    BOTANICAL_GREEN("botanical_green", "Leaf Green", 0xFF0E7A1DL),
    ROYAL_INDIGO("royal_indigo", "Royal Indigo", 0xFF2B4CB8L),
    OCEAN_TEAL("ocean_teal", "Ocean Teal", 0xFF00796BL),
    CRIMSON_RED("crimson_red", "Crimson", 0xFFB8253AL),
    AMBER_GOLD("amber_gold", "Amber Gold", 0xFFA65E00L),
    DEEP_VIOLET("deep_violet", "Deep Violet", 0xFF6A2CA0L),
    COBALT_SKY("cobalt_sky", "Cobalt Blue", 0xFF0277BDL),
    CHARCOAL_SLATE("charcoal_slate", "Slate Dark", 0xFF263238L),
    ;

    companion object {
        fun findMatchingPreset(colorHex: Long): AppThemePreset? =
            entries.firstOrNull { (it.primaryHex and 0x00FFFFFFL) == (colorHex and 0x00FFFFFFL) }

        fun parseHexColor(rawInput: String): Long? {
            val cleaned = rawInput.trim().removePrefix("#").removePrefix("0x").removePrefix("0X")
            if (cleaned.length != 6 || !cleaned.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
                return null
            }
            val rgb = cleaned.toLongOrNull(16) ?: return null
            return 0xFF000000L or rgb
        }

        fun formatHexColor(colorHex: Long): String =
            "#%06X".format(colorHex and 0x00FFFFFFL)
    }
}

data class CompanyItem(
    val id: String,
    val name: String,
    val businessCardUrl: String,
    val websiteUrl: String,
    val themeColorHex: Long = AppThemePreset.BOTANICAL_GREEN.primaryHex,
)

data class CardProfile(
    val fullName: String = DEFAULT_FULL_NAME,
    val initials: String = DEFAULT_INITIALS,
    val title: String = DEFAULT_TITLE,
    val organization: String = DEFAULT_ORGANIZATION,
    val tagline: String = DEFAULT_TAGLINE,
    val phone: String = DEFAULT_PHONE,
    val whatsappPhone: String = DEFAULT_WHATSAPP_PHONE,
    val email: String = DEFAULT_EMAIL,
    val websiteUrl: String = DEFAULT_WEBSITE_URL,
    val cardShareUrl: String = DEFAULT_CARD_SHARE_URL,
    val address: String = DEFAULT_ADDRESS,
    val secondaryAddress: String = DEFAULT_SECONDARY_ADDRESS,
    val locationMapUrl: String = DEFAULT_LOCATION_MAP_URL,
    val instagramUrl: String = DEFAULT_INSTAGRAM_URL,
    val facebookUrl: String = DEFAULT_FACEBOOK_URL,
    val youtubeUrl: String = DEFAULT_YOUTUBE_URL,
    val bio: String = DEFAULT_BIO,
) {
    companion object {
        const val DEFAULT_FULL_NAME = "Dr. V.K. Abdul Samad"
        const val DEFAULT_INITIALS = "AS"
        const val DEFAULT_TITLE = "Managing Director"
        const val LEGACY_DEFAULT_ORGANIZATION =
            "Traditional Treatment & Research Institute Private Limited"
        const val DEFAULT_ORGANIZATION =
            "Samad's Elements Traditional Treatment & Research Institute Private Limited"
        const val DEFAULT_TAGLINE = "Samad's Elements • Traditional Treatment Centre"
        const val DEFAULT_PHONE = "8921172611"
        const val DEFAULT_WHATSAPP_PHONE = "8590946006"
        const val DEFAULT_EMAIL = ""
        const val DEFAULT_WEBSITE_URL = "https://samadselements.com/"
        const val DEFAULT_CARD_SHARE_URL =
            "https://sites.google.com/view/dr-v-k-abdul-samad-digital-bus/full-page-embed"
        const val DEFAULT_ADDRESS =
            "Near Thiruvallam Foot Over Bridge, Poonthura P.O., Thiruvananthapuram, Kerala 695026"
        const val DEFAULT_SECONDARY_ADDRESS = "Mukathala P.O., Kollam, Kerala 691577"
        const val DEFAULT_LOCATION_MAP_URL = "https://maps.app.goo.gl/RfDo3imBc6Y4kxfQ8"
        const val DEFAULT_INSTAGRAM_URL = "https://www.instagram.com/samads_elements/?hl=en"
        const val DEFAULT_FACEBOOK_URL = "https://www.facebook.com/elementtraditional/"
        const val DEFAULT_YOUTUBE_URL = "https://www.youtube.com/@SamadElementTraditional"
        const val DEFAULT_BIO =
            "Managing Director at Samad's Elements Traditional Treatment & Research Institute Private Limited, serving patients and communities across Thiruvananthapuram and Kollam."
    }
}

enum class WhatsAppAppPreference(val label: String) {
    AUTO("Auto (WhatsApp / Business)"),
    WHATSAPP("WhatsApp"),
    WHATSAPP_BUSINESS("WhatsApp Business"),
}

data class ShareSettings(
    val defaultCountryCode: String = DEFAULT_COUNTRY_CODE,
    val greetingTemplate: String = DEFAULT_GREETING_TEMPLATE,
    val preferredWhatsAppApp: WhatsAppAppPreference = WhatsAppAppPreference.AUTO,
    val savedCompanies: List<CompanyItem> = DEFAULT_COMPANIES,
    val activeCompanyId: String = DEFAULT_COMPANY_ID,
) {
    val activeCompany: CompanyItem
        get() = savedCompanies.firstOrNull { it.id == activeCompanyId }
            ?: savedCompanies.firstOrNull()
            ?: DEFAULT_COMPANIES.first()

    companion object {
        const val DEFAULT_COUNTRY_CODE = "+91"
        const val LEGACY_DEFAULT_GREETING_TEMPLATE =
            "Greetings{name}, thank you for connecting with Samad's Elements. Please find my digital business card below:"
        const val DEFAULT_GREETING_TEMPLATE =
            "Greetings{name}, thank you for connecting with {company}. Please find my digital business card below:"
        const val DEFAULT_COMPANY_ID = "company_samads_elements"
        val DEFAULT_COMPANIES = listOf(
            CompanyItem(
                id = DEFAULT_COMPANY_ID,
                name = "Samad's Elements",
                businessCardUrl = CardProfile.DEFAULT_CARD_SHARE_URL,
                websiteUrl = CardProfile.DEFAULT_WEBSITE_URL,
                themeColorHex = AppThemePreset.BOTANICAL_GREEN.primaryHex,
            ),
        )
    }
}

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    OTHER,
}

data class CallLogEntry(
    val id: String,
    val displayName: String?,
    val phoneNumber: String,
    val callType: CallType,
    val timestampMillis: Long,
    val durationSeconds: Long,
)

data class ContactEntry(
    val id: String,
    val displayName: String,
    val phoneNumber: String,
    val label: String,
)

enum class ShareTab {
    RECENT_CALLS,
    CONTACTS,
}
