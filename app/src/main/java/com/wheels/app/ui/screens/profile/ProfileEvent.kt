package com.wheels.app.ui.screens.profile

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
}
