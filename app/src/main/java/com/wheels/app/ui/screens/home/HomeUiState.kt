package com.wheels.app.ui.screens.home

import androidx.compose.ui.graphics.Color

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Maria",
    val welcomeMessage: String = "Welcome back",
    val quickStats: List<HomeQuickStat> = listOf(
        HomeQuickStat(label = "Rides", value = "12"),
        HomeQuickStat(label = "Reliability", value = "98%", accentColor = Color(0xFF10B981)),
        HomeQuickStat(label = "Rating", value = "5.0")
    ),
    val activeRide: ActiveRideUiModel = ActiveRideUiModel(),
    val updates: List<HomeUpdateUiModel> = listOf(
        HomeUpdateUiModel(
            title = "Driver arriving soon",
            description = "Carlos is 3 minutes away from pickup",
            timestamp = "Now",
            tone = UpdateTone.Success
        ),
        HomeUpdateUiModel(
            title = "You earned punctuality points!",
            description = "+5 points for being on time",
            timestamp = "5m",
            tone = UpdateTone.Info
        )
    )
)

data class HomeQuickStat(
    val label: String,
    val value: String,
    val accentColor: Color? = null
)

data class ActiveRideUiModel(
    val driver: String = "Carlos Mendez",
    val rating: String = "4.8",
    val carModel: String = "Toyota Corolla 2020",
    val licensePlate: String = "ABC-123",
    val pickupLocation: String = "Campus Uniandes - Entrance Gate",
    val destination: String = "Centro Comercial Andino",
    val etaMinutes: Int = 3,
    val fare: String = "$3,500",
    val distance: String = "4.2 km",
    val routeProgress: Int = 45
)

data class HomeUpdateUiModel(
    val title: String,
    val description: String,
    val timestamp: String,
    val tone: UpdateTone
)

enum class UpdateTone {
    Success,
    Info
}
