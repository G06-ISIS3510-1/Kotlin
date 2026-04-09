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

data class ThemeSettingsUiState(
    val isDarkModeEnabled: Boolean = false,
    val isAdaptiveThemeEnabled: Boolean = false,
    val effectiveDarkTheme: Boolean = false,
    val ambientLux: Float? = null,
    val isLightSensorAvailable: Boolean = false
)

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository,
    ambientLightMonitor: AmbientLightMonitor
) : ViewModel() {

    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        themePreferencesRepository.themePreferences,
        ambientLightMonitor.ambientLightState
    ) { preferences, ambientLightState ->
        val effectiveDarkTheme = when {
            preferences.isAdaptiveThemeEnabled && ambientLightState.isLightSensorAvailable ->
                (ambientLightState.ambientLux ?: Float.MAX_VALUE) < LOW_LIGHT_THRESHOLD_LUX
            else -> preferences.isDarkModeEnabled
        }

        ThemeSettingsUiState(
            isDarkModeEnabled = preferences.isDarkModeEnabled,
            isAdaptiveThemeEnabled = preferences.isAdaptiveThemeEnabled,
            effectiveDarkTheme = effectiveDarkTheme,
            ambientLux = ambientLightState.ambientLux,
            isLightSensorAvailable = ambientLightState.isLightSensorAvailable
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettingsUiState()
        )

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setDarkModeEnabled(enabled)
        }
    }

    fun setAdaptiveThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setAdaptiveThemeEnabled(enabled)
        }
    }

    private companion object {
        const val LOW_LIGHT_THRESHOLD_LUX = 30f
    }
}
