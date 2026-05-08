package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class DriverRideRecord(
    val id: String,
    val driverId: String,
    val origin: String,
    val originCoordinates: Coordinates? = null,
    val destination: String,
    val destinationCoordinates: Coordinates? = null,
    val departureAt: Instant,
    val estimatedDurationMinutes: Int,
    val availableSeats: Int,
    val totalSeats: Int,
    val pricePerSeat: Int,
    val carModel: String,
    val licensePlate: String,
    val notes: String,
    val driverName: String,
    val driverEmail: String,
    val status: String,
    val paymentOption: String = "card"
)
