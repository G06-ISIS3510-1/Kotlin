package com.wheels.app.navigation

sealed class Destinations(val route: String) {
    data object Home : Destinations("home")
    data object Rides : Destinations("rides")
    data object Payments : Destinations("payments")
    data object Profile : Destinations("profile")
}
