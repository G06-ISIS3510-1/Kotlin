package com.wheels.app.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeSettingsUiState(
    val isDarkModeEnabled: Boolean = false
)

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<ThemeSettingsUiState> = themePreferencesRepository.isDarkModeEnabled
        .map { isDarkModeEnabled -> ThemeSettingsUiState(isDarkModeEnabled = isDarkModeEnabled) }
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
}
