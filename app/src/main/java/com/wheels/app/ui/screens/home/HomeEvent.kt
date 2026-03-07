package com.wheels.app.ui.screens.home

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}
