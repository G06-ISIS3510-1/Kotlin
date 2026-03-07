package com.wheels.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = WheelsSurface,
    secondary = SecondaryBlue,
    onSecondary = WheelsSurface,
    tertiary = ElectricGreen,
    background = WheelsBackground,
    onBackground = TextPrimary,
    surface = WheelsSurface,
    onSurface = TextPrimary,
    error = WheelsError,
    outline = Border
)

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryBlue,
    secondary = PrimaryBlue,
    tertiary = ElectricGreen,
    background = TextPrimary,
    onBackground = WheelsSurface,
    surface = ColorTokens.DarkSurface,
    onSurface = WheelsSurface,
    error = WheelsError,
    outline = ColorTokens.DarkOutline
)

private object ColorTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF111827)
    val DarkOutline = androidx.compose.ui.graphics.Color(0xFF334155)
}

@Composable
fun WheelsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WheelsTypography,
        shapes = WheelsShapes,
        content = content
    )
}
