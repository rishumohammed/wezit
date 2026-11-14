package com.example

import com.example.model.AppThemePreset
import com.example.model.CardProfile
import com.example.model.ShareSettings
import com.example.util.IntentLauncher
import com.example.util.PhoneFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneFormatterTest {

    @Test
    fun `normalizeCountryCode formats digits with leading plus`() {
        assertEquals("+91", PhoneFormatter.normalizeCountryCode("91"))
        assertEquals("+971", PhoneFormatter.normalizeCountryCode(" +971 "))
        assertEquals("+91", PhoneFormatter.normalizeCountryCode(""))
    }

    @Test
    fun `formatForWhatsApp normalizes local, trunk-zero, and international numbers`() {
        assertEquals("919876543210", PhoneFormatter.formatForWhatsApp("98765 43210", "+91"))
        assertEquals("919876543210", PhoneFormatter.formatForWhatsApp("098765-43210", "+91"))
        assertEquals("919876543210", PhoneFormatter.formatForWhatsApp("919876543210", "+91"))
        assertEquals("971501234567", PhoneFormatter.formatForWhatsApp("+971 50 123 4567", "+91"))
        assertEquals("447911123456", PhoneFormatter.formatForWhatsApp("0044 7911 123456", "+91"))
        assertNull(PhoneFormatter.formatForWhatsApp("123", "+91"))
        assertNull(PhoneFormatter.formatForWhatsApp("", "+91"))
    }

    @Test
    fun `buildWhatsAppMessage interpolates recipient name cleanly and appends link`() {
        val withName = PhoneFormatter.buildWhatsAppMessage(
            greetingTemplate = "Greetings{name}, here is my card:",
            cardShareUrl = "https://example.com/card",
            recipientName = "Dr. Ahmed",
        )
        assertEquals(
            "Greetings Dr. Ahmed, here is my card:\n\nhttps://example.com/card",
            withName,
        )

        val withoutName = PhoneFormatter.buildWhatsAppMessage(
            greetingTemplate = "Greetings{name}, here is my card:",
            cardShareUrl = "https://example.com/card",
            recipientName = null,
        )
        assertEquals(
            "Greetings, here is my card:\n\nhttps://example.com/card",
            withoutName,
        )
    }

    @Test
    fun `buildShareCardText includes full Samad's Elements organization name`() {
        val actual = IntentLauncher.buildShareCardText(
            profile = CardProfile(),
            greetingTemplate = ShareSettings.DEFAULT_GREETING_TEMPLATE,
        )
        val expected = """
            Dr. V.K. Abdul Samad
            Managing Director
            Samad's Elements Traditional Treatment & Research Institute Private Limited
            Phone: 8921172611
            WhatsApp: 8590946006
            Website: https://samadselements.com/
            Location: https://maps.app.goo.gl/RfDo3imBc6Y4kxfQ8

            Greetings, thank you for connecting with Samad's Elements. Please find my digital business card below:

            https://sites.google.com/view/dr-v-k-abdul-samad-digital-bus/full-page-embed
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `theme hex parser handles valid and invalid hex codes`() {
        assertEquals(0xFF0E7A1DL, AppThemePreset.parseHexColor("#0E7A1D"))
        assertEquals(0xFF2B4CB8L, AppThemePreset.parseHexColor("2b4cb8"))
        assertEquals("#0E7A1D", AppThemePreset.formatHexColor(0xFF0E7A1DL))
        assertNull(AppThemePreset.parseHexColor("#123"))
        assertNull(AppThemePreset.parseHexColor("ZZZZZZ"))
    }

    @Test
    fun `switching company updates WhatsApp and Share Card messages automatically`() {
        val whatsappMessage = PhoneFormatter.buildWhatsAppMessage(
            greetingTemplate = ShareSettings.DEFAULT_GREETING_TEMPLATE,
            cardShareUrl = "https://wezit.in/card",
            recipientName = "Dr. Ahmed",
            companyName = "WEZIT Studio",
        )
        assertEquals(
            "Greetings Dr. Ahmed, thank you for connecting with WEZIT Studio. Please find my digital business card below:\n\nhttps://wezit.in/card",
            whatsappMessage,
        )

        val switchedProfile = CardProfile().copy(
            organization = "WEZIT Studio",
            websiteUrl = "https://wezit.in",
            cardShareUrl = "https://wezit.in/card",
        )
        val shareText = IntentLauncher.buildShareCardText(
            profile = switchedProfile,
            greetingTemplate = ShareSettings.DEFAULT_GREETING_TEMPLATE,
            shareUrl = "https://wezit.in/card",
            companyName = "WEZIT Studio",
        )
        assertEquals(
            """
            Dr. V.K. Abdul Samad
            Managing Director
            WEZIT Studio
            Phone: 8921172611
            WhatsApp: 8590946006
            Website: https://wezit.in
            Location: https://maps.app.goo.gl/RfDo3imBc6Y4kxfQ8

            Greetings, thank you for connecting with WEZIT Studio. Please find my digital business card below:

            https://wezit.in/card
            """.trimIndent(),
            shareText,
        )
    }
}
