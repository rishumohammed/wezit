package com.example.util

object PhoneFormatter {

    /**
     * Normalizes a user-entered country code into "+<digits>" format.
     * Falls back to "+91" if no digits are provided.
     */
    fun normalizeCountryCode(rawCode: String): String {
        val digits = rawCode.filter { it.isDigit() }.take(4)
        return if (digits.isEmpty()) "+91" else "+$digits"
    }

    /**
     * Formats any raw local or international phone number into digits-only E.164 format
     * suitable for WhatsApp deep links (e.g., "919876543210").
     * Returns null if the number does not contain enough digits.
     */
    fun formatForWhatsApp(rawNumber: String, defaultCountryCode: String): String? {
        val trimmed = rawNumber.trim()
        if (trimmed.isEmpty()) return null

        val ccDigits = normalizeCountryCode(defaultCountryCode).drop(1)
        val hasExplicitPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length < 6) return null

        val resolved = when {
            hasExplicitPlus -> digitsOnly
            trimmed.startsWith("00") && digitsOnly.length > 4 -> digitsOnly.drop(2)
            else -> {
                val strippedTrunk = digitsOnly.trimStart('0')
                if (strippedTrunk.length < 6) return null
                // If the number already includes the country code + 10-digit subscriber number
                if (strippedTrunk.length >= ccDigits.length + 10 &&
                    strippedTrunk.startsWith(ccDigits)
                ) {
                    strippedTrunk
                } else {
                    "$ccDigits$strippedTrunk"
                }
            }
        }

        return if (resolved.length in 7..15) resolved else null
    }

    fun formatDisplayWhatsApp(rawNumber: String, defaultCountryCode: String): String? {
        val digits = formatForWhatsApp(rawNumber, defaultCountryCode) ?: return null
        return "+$digits"
    }

    /**
     * Builds the WhatsApp message by interpolating optional {name} and {company} placeholders
     * and appending the active digital business card link.
     */
    fun buildWhatsAppMessage(
        greetingTemplate: String,
        cardShareUrl: String,
        recipientName: String?,
        companyName: String? = "Samad's Elements",
    ): String {
        val cleanName = recipientName?.trim().orEmpty()
        val cleanCompany = companyName?.trim().orEmpty().ifEmpty { "us" }
        val withName = if (cleanName.isNotEmpty()) {
            greetingTemplate.replace("{name}", " $cleanName")
        } else {
            greetingTemplate
                .replace(" {name}", "")
                .replace("{name}", "")
        }
        val renderedGreeting = withName
            .replace("{company}", cleanCompany)
            .replace(Regex(" {2,}"), " ")
            .trim()

        val cleanUrl = cardShareUrl.trim()
        return when {
            renderedGreeting.isNotEmpty() && cleanUrl.isNotEmpty() ->
                "$renderedGreeting\n\n$cleanUrl"
            renderedGreeting.isNotEmpty() -> renderedGreeting
            else -> cleanUrl
        }
    }
}
