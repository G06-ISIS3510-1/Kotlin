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

// Domain model for theme configuration stored in SharedPreferences
// isDarkModeEnabled: manual dark mode, isAdaptiveThemeEnabled: light sensor-based theme
data class ThemePreferences(
    val isDarkModeEnabled: Boolean = false,
    val isAdaptiveThemeEnabled: Boolean = false
)

/**
 * Repository for persisting and managing theme preferences.
 * Reads from SharedPreferences, exposes as reactive Flow for subscribers.
 */
@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {
    // Initialize SharedPreferences with private mode (only this app can access)
    // Named "theme_preferences" to isolate theme data from other preferences
    private val sharedPreferences = context.getSharedPreferences(
        THEME_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    // In-memory reactive state: initialized by reading current preferences from disk
    private val preferencesState = MutableStateFlow(readPreferences())

    // Public Flow API: consumers subscribe here to receive preference updates
    // Using asStateFlow() makes it read-only from outside, preventing external modifications
    val themePreferences: Flow<ThemePreferences> = preferencesState.asStateFlow()

    // Persist dark mode preference to SharedPreferences and update Flow
    // Runs on IO Dispatcher to avoid blocking main thread
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            // Persist to disk and check if write was successful
            val didPersist = sharedPreferences.edit()
                .putBoolean(DARK_MODE_ENABLED, enabled)
                .commit()  // commit() = synchronous write

            // Only update in-memory state if persistence succeeded
            // This ensures state reflects what's actually on disk
            if (didPersist) {
                preferencesState.value = preferencesState.value.copy(isDarkModeEnabled = enabled)
            }
        }
    }

    // Persist adaptive theme preference to SharedPreferences and update Flow
    // When enabled, light sensor controls theme; otherwise manual preference used
    suspend fun setAdaptiveThemeEnabled(enabled: Boolean) {
        withContext(ioDispatcher) {
            // Persist to disk and check if write was successful
            val didPersist = sharedPreferences.edit()
                .putBoolean(ADAPTIVE_THEME_ENABLED, enabled)
                .commit()  // commit() = synchronous write

            // Only update in-memory state if persistence succeeded
            if (didPersist) {
                preferencesState.value = preferencesState.value.copy(isAdaptiveThemeEnabled = enabled)
            }
        }
    }

    /**
     * Reads theme preferences from SharedPreferences into domain model.
     * Called on app startup to initialize in-memory state.
     *
     * BEHAVIOR:
     * - Default values (false) are used if keys don't exist in SharedPreferences
     * - Runs synchronously but on IO thread to avoid ANR
     *
     * @return ThemePreferences with values from disk or defaults
     */
    // Read theme preferences from SharedPreferences into domain model
    // Uses default values if keys don't exist
    private fun readPreferences(): ThemePreferences {
        return ThemePreferences(
            isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_ENABLED, false),
            isAdaptiveThemeEnabled = sharedPreferences.getBoolean(ADAPTIVE_THEME_ENABLED, false)
        )
    }

    private companion object {
        // SharedPreferences file name - used to isolate theme data from other app preferences
        const val THEME_PREFERENCES_NAME = "theme_preferences"
        
        // Key for dark mode enabled flag in SharedPreferences
        const val DARK_MODE_ENABLED = "dark_mode_enabled"
        
        // Key for adaptive theme enabled flag in SharedPreferences
        // When true: theme adapts based on ambient light sensor readings
        // When false: theme follows isDarkModeEnabled user choice
        const val ADAPTIVE_THEME_ENABLED = "adaptive_theme_enabled"
    }
}
