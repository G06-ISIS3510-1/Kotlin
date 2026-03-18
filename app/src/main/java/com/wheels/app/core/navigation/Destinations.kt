package com.wheels.app.core.navigation

sealed class Destinations(val route: String) {
    data object Home : Destinations("home")
    data object Rides : Destinations("rides")
    data object Payments : Destinations("payments")
    data object QuickPayment : Destinations("quick_payment")
    data object GroupChat : Destinations("group_chat")
    data object Profile : Destinations("profile")
}
