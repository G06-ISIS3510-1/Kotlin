package com.wheels.app.core.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class ThemePreferences(
    val isDarkModeEnabled: Boolean = false,
    val isAdaptiveThemeEnabled: Boolean = false
)

@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val sharedPreferences = context.getSharedPreferences(
        THEME_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val preferencesState = MutableStateFlow(readPreferences())

    val themePreferences: Flow<ThemePreferences> = preferencesState.asStateFlow()

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            val didPersist = sharedPreferences.edit()
                .putBoolean(DARK_MODE_ENABLED, enabled)
                .commit()

            if (didPersist) {
                preferencesState.value = preferencesState.value.copy(isDarkModeEnabled = enabled)
            }
        }
    }

    suspend fun setAdaptiveThemeEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            val didPersist = sharedPreferences.edit()
                .putBoolean(ADAPTIVE_THEME_ENABLED, enabled)
                .commit()

            if (didPersist) {
                preferencesState.value = preferencesState.value.copy(isAdaptiveThemeEnabled = enabled)
            }
        }
    }

    private fun readPreferences(): ThemePreferences {
        return ThemePreferences(
            isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_ENABLED, false),
            isAdaptiveThemeEnabled = sharedPreferences.getBoolean(ADAPTIVE_THEME_ENABLED, false)
        )
    }

    private companion object {
        const val THEME_PREFERENCES_NAME = "theme_preferences"
        const val DARK_MODE_ENABLED = "dark_mode_enabled"
        const val ADAPTIVE_THEME_ENABLED = "adaptive_theme_enabled"
    }
}
