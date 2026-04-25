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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* No-op: the service checks permission before posting notifications. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            val themeViewModel: ThemeSettingsViewModel = hiltViewModel()
            val themeState = themeViewModel.uiState.collectAsStateWithLifecycle()

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
