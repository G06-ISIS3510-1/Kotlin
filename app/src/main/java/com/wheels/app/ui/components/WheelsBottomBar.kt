package com.wheels.app.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable

@Composable
fun WheelsBottomBar(content: @Composable () -> Unit) {
    NavigationBar {
        content()
    }
}
