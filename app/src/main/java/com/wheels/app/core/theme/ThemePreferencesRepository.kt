package com.wheels.app.core.theme

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

data class ThemePreferences(
    val isDarkModeEnabled: Boolean = false,
    val isAdaptiveThemeEnabled: Boolean = false
)

@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("theme_preferences") }
    )

    val themePreferences: Flow<ThemePreferences> = dataStore.data
        .map { preferences ->
            ThemePreferences(
                isDarkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
                isAdaptiveThemeEnabled = preferences[ADAPTIVE_THEME_ENABLED] ?: false
            )
        }
        .distinctUntilChanged()

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setAdaptiveThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADAPTIVE_THEME_ENABLED] = enabled
        }
    }

    private companion object {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val ADAPTIVE_THEME_ENABLED = booleanPreferencesKey("adaptive_theme_enabled")
    }
}
