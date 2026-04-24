package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class Ride(
    val id: String,
    val driverId: String,
    val origin: String,
    val originCoordinates: Coordinates? = null,
    val destination: String,
    val destinationCoordinates: Coordinates? = null,
    val destinationArea: String = "",
    val departureTime: Instant,
    val availableSeats: Int,
    val pricePerSeat: Double
)
