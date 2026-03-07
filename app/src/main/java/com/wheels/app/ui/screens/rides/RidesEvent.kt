package com.wheels.app.ui.screens.rides

sealed interface RidesEvent {
    data object LoadRides : RidesEvent
}
