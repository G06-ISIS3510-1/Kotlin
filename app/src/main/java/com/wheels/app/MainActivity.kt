package com.wheels.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wheels.app.navigation.WheelsNavGraph
import com.wheels.app.ui.theme.WheelsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WheelsTheme {
                WheelsNavGraph()
            }
        }
    }
}
