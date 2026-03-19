package com.wheels.app.core.navigation

sealed class Destinations(val route: String) {
    data object Home : Destinations("home")
    data object Rides : Destinations("rides")
    data object Payments : Destinations("payments")
    data object QuickPayment : Destinations("quick_payment")
    data object RideRequest : Destinations("ride_request/{rideId}") {
        fun createRoute(rideId: String): String = "ride_request/$rideId"
    }
    data object BookingConfirmation : Destinations("booking_confirmation/{rideId}/{seats}") {
        fun createRoute(rideId: String, seats: Int): String = "booking_confirmation/$rideId/$seats"
    }
    data object GroupChat : Destinations("group_chat")
    data object Profile : Destinations("profile")
}
