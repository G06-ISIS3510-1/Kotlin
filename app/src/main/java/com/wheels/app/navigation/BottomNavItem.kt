package com.wheels.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val usesAvatar: Boolean = false
)

val wheelsBottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Destinations.Home.route,
        icon = Icons.Outlined.Place
    ),
    BottomNavItem(
        label = "Rides",
        route = Destinations.Rides.route,
        icon = Icons.Outlined.Navigation
    ),
    BottomNavItem(
        label = "Alerts",
        route = Destinations.Payments.route,
        icon = Icons.Outlined.NotificationsNone
    ),
    BottomNavItem(
        label = "Profile",
        route = Destinations.Profile.route,
        icon = Icons.Outlined.Place,
        usesAvatar = true
    )
)
