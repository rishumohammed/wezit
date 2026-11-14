package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches WEZIT`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WEZIT", appName)
    }

    @Test
    fun `navigation and multi-company controls across Quick Share, My Card, and Settings work`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("grant_permission_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manual_number_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manual_send_whatsapp_button").assertIsDisplayed()

        // Switch between Recent Calls and Contacts tabs
        composeTestRule.onNodeWithTag("tab_contacts").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tab_recent_calls").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Navigate to My Card
        composeTestRule.onNodeWithTag("nav_my_card").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("edit_card_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_code_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_contact_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("social_instagram_button").performScrollTo().assertIsDisplayed()

        // Navigate to Settings and verify multi-company controls
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("add_company_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("company_row_company_samads_elements").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_country_code_button").performScrollTo().assertIsDisplayed()
    }
}
