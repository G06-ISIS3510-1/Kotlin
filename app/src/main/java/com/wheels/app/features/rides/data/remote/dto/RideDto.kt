package com.wheels.app.features.rides.data.remote.dto

data class RideDto(
    val id: String,
    val driverId: String,
    val origin: String,
    val destination: String,
    val departureTimeIso: String,
    val availableSeats: Int,
    val pricePerSeat: Double
)
