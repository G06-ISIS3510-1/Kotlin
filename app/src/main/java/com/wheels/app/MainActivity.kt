package com.wheels.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wheels.app.core.navigation.WheelsNavGraph
import com.wheels.app.core.theme.ThemeSettingsViewModel
import com.wheels.app.core.ui.theme.WheelsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeSettingsViewModel = hiltViewModel()
            val themeState = themeViewModel.uiState.collectAsStateWithLifecycle()

            WheelsTheme(darkTheme = themeState.value.effectiveDarkTheme) {
                WheelsNavGraph(themeViewModel = themeViewModel)
            }
        }
    }
}
