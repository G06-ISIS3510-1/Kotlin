package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class Ride(
    val id: String,
    val driverId: String,
    val origin: String,
    val destination: String,
    val departureTime: Instant,
    val availableSeats: Int,
    val pricePerSeat: Double
)
