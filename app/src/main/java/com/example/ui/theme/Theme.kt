package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private fun buildLightColorScheme(accentColor: Color): ColorScheme {
    // Ensure high legibility on crisp white surfaces even if a custom light color is picked
    val primary = if (accentColor.luminance() > 0.55f) {
        lerp(accentColor, Color.Black, 0.35f)
    } else {
        accentColor
    }
    val onPrimary = if (primary.luminance() > 0.50f) CharcoalHeading else Color.White
    val darkerAccent = lerp(primary, Color.Black, 0.25f)
    val softTint = lerp(Color.White, primary, 0.10f)
    val subtleCanvasTint = lerp(Color.White, primary, 0.035f)
    val highTint = lerp(Color.White, primary, 0.18f)
    val outlineTint = lerp(Color(0xFFE3E7E4), primary, 0.14f)
    val dividerTint = lerp(Color(0xFFEDF0EE), primary, 0.08f)

    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = darkerAccent,
        onPrimaryContainer = Color.White,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = softTint,
        onSecondaryContainer = darkerAccent,
        tertiary = primary,
        onTertiary = onPrimary,
        background = subtleCanvasTint,
        onBackground = CharcoalHeading,
        surface = subtleCanvasTint,
        onSurface = CharcoalHeading,
        surfaceVariant = subtleCanvasTint,
        onSurfaceVariant = SlateBodyText,
        surfaceContainerLowest = PureCardWhite,
        surfaceContainerLow = PureCardWhite,
        surfaceContainer = subtleCanvasTint,
        surfaceContainerHigh = softTint,
        surfaceContainerHighest = highTint,
        outline = outlineTint,
        outlineVariant = dividerTint,
        error = MissedCallCrimson,
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SamadCardTheme(
    accentColor: Color = BotanicalGreen,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(accentColor) {
        buildLightColorScheme(accentColor)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
