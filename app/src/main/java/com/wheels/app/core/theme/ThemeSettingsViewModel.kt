package com.wheels.app.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// UI state for theme settings: includes preferences, sensor data, and computed effective theme
// effectiveDarkTheme: actual theme being applied (based on adaptive logic or manual setting)
data class ThemeSettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val isAdaptiveThemeEnabled: Boolean = false,
    val effectiveDarkTheme: Boolean = false,
    val ambientLux: Float? = null,
    val isLightSensorAvailable: Boolean = false
)

/**
 * Manages theme settings and determines effective theme based on preferences and sensor data.
 * Combines ThemePreferencesRepository (user prefs) and AmbientLightMonitor (light sensor).
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository,
    ambientLightMonitor: AmbientLightMonitor
) : ViewModel() {

    /**
     * Combines preferences (SharedPreferences) and sensor readings (light sensor) to compute effective theme.
     * MainActivity and UiThemeScreen subscribe to this to get current theme state.
     */
    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        themePreferencesRepository.themePreferences,  // Flow from SharedPreferences
        ambientLightMonitor.ambientLightState        // Flow from light sensor
    ) { preferences, ambientLightState ->
        // Determine which theme to apply: if adaptive enabled + sensor available, use light sensor
        // Otherwise use manual isDarkModeEnabled preference
        val effectiveDarkTheme = when {
            preferences.isAdaptiveThemeEnabled && ambientLightState.isLightSensorAvailable ->
                (ambientLightState.ambientLux ?: Float.MAX_VALUE) < LOW_LIGHT_THRESHOLD_LUX
            else -> preferences.isDarkModeEnabled  // Use manual preference
        }

        // Build complete UI state to expose to UI layer
        ThemeSettingsUiState(
            isDarkModeEnabled = preferences.isDarkModeEnabled,
            isAdaptiveThemeEnabled = preferences.isAdaptiveThemeEnabled,
            effectiveDarkTheme = effectiveDarkTheme,  // This is what MainActivity uses!
            ambientLux = ambientLightState.ambientLux,
            isLightSensorAvailable = ambientLightState.isLightSensorAvailable
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),  // Cache for 5 seconds when no subscribers
            initialValue = ThemeSettingsUiState()
        )

    // Save dark mode preference to SharedPreferences, triggers theme recompute and app recomposition
    // Notifies MainActivity and UiThemeScreen of the change
    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setDarkModeEnabled(enabled)
        }
    }

    // Save adaptive theme preference to SharedPreferences, light sensor now controls theme if enabled
    // Dark mode toggle becomes disabled in UI when this is turned on
    fun setAdaptiveThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setAdaptiveThemeEnabled(enabled)
        }
    }

    private companion object {
        // Ambient light threshold for deciding dark vs light theme
        // Values below 30 lux indicate low light (night/indoor), so use dark theme
        // Values >= 30 lux indicate normal/bright light, so use light theme
        const val LOW_LIGHT_THRESHOLD_LUX = 30f
    }
}
