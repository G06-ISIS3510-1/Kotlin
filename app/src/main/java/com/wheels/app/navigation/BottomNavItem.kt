package com.wheels.app.navigation

data class BottomNavItem(
    val label: String,
    val route: String,
    val badgeCount: Int? = null
)

val wheelsBottomNavItems = listOf(
    BottomNavItem(label = "Home", route = Destinations.Home.route),
    BottomNavItem(label = "Mis Viajes", route = Destinations.Rides.route),
    BottomNavItem(label = "Pagos", route = Destinations.Payments.route),
    BottomNavItem(label = "Perfil", route = Destinations.Profile.route)
)
