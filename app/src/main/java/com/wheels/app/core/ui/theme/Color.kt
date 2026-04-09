package com.wheels.app.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private const val LIGHT_PRIMARY_BLUE = 0xFF1A3A5C
private const val DARK_PRIMARY_BLUE = 0xFFBFDBFE
private const val LIGHT_SECONDARY_BLUE = 0xFF2563EB
private const val DARK_SECONDARY_BLUE = 0xFF60A5FA
private const val LIGHT_WHEELS_BACKGROUND = 0xFFF8FAFC
private const val DARK_WHEELS_BACKGROUND = 0xFF0F172A
private const val LIGHT_WHEELS_SURFACE = 0xFFFFFFFF
private const val DARK_WHEELS_SURFACE = 0xFF111827
private const val LIGHT_TEXT_PRIMARY = 0xFF0F172A
private const val DARK_TEXT_PRIMARY = 0xFFF8FAFC
private const val LIGHT_TEXT_SECONDARY = 0xFF64748B
private const val DARK_TEXT_SECONDARY = 0xFFCBD5E1
private const val LIGHT_BORDER = 0xFFE2E8F0
private const val DARK_BORDER = 0xFF334155

private var isDarkThemePaletteEnabled by mutableStateOf(false)

internal fun setThemePaletteDarkMode(isDarkThemeEnabled: Boolean) {
    isDarkThemePaletteEnabled = isDarkThemeEnabled
}

val PrimaryBlue: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_PRIMARY_BLUE else LIGHT_PRIMARY_BLUE)

val SecondaryBlue: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_SECONDARY_BLUE else LIGHT_SECONDARY_BLUE)

val ElectricGreen = Color(0xFF10B981)

val WheelsBackground: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_WHEELS_BACKGROUND else LIGHT_WHEELS_BACKGROUND)

val WheelsSurface: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_WHEELS_SURFACE else LIGHT_WHEELS_SURFACE)

val TextPrimary: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_TEXT_PRIMARY else LIGHT_TEXT_PRIMARY)

val TextSecondary: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_TEXT_SECONDARY else LIGHT_TEXT_SECONDARY)

val Warning = Color(0xFFF59E0B)
val WheelsError = Color(0xFFEF4444)

val Border: Color
    get() = Color(if (isDarkThemePaletteEnabled) DARK_BORDER else LIGHT_BORDER)
