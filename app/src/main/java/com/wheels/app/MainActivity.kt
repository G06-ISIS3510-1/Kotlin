package com.wheels.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wheels.app.core.navigation.WheelsNavGraph
import com.wheels.app.core.theme.ThemeSettingsViewModel
import com.wheels.app.core.ui.theme.WheelsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity: sets up Compose content and applies the computed theme to entire app.
 * Subscribes to ThemeSettingsViewModel to get effectiveDarkTheme and reapply when it changes.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* No-op: the service checks permission before posting notifications. */ }

    // Initialize Compose with theme management
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        // Set Compose as the main UI framework
        setContent {
        // Get ThemeSettingsViewModel and subscribe to theme state
        // Hilt automatically injects ThemePreferencesRepository and AmbientLightMonitor
        val themeViewModel: ThemeSettingsViewModel = hiltViewModel()
            
        // Collect theme state and apply to entire app
        // effectiveDarkTheme: computed based on adaptive logic or manual preference
        val themeState = themeViewModel.uiState.collectAsStateWithLifecycle()

        // Apply effectiveDarkTheme to entire app via WheelsTheme wrapper
        WheelsTheme(darkTheme = themeState.value.effectiveDarkTheme) {
            WheelsNavGraph(themeViewModel = themeViewModel)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Request once when the main app shell opens so incoming FCM messages
        // can be rendered without coupling permission flow to messaging service code.
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
