package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class Ride(
    val id: String,
    val driverId: String,
    val driverName: String = "",
    val driverEmail: String = "",
    val driverRating: Double = 5.0,
    val reviewCount: Int = 0,
    val reliabilityScore: Int = 100,
    val status: String = "published",
    val origin: String,
    val originCoordinates: Coordinates? = null,
    val destination: String,
    val destinationCoordinates: Coordinates? = null,
    val destinationArea: String = "",
    val departureTime: Instant,
    val estimatedDurationMinutes: Int = 30,
    val availableSeats: Int,
    val totalSeats: Int = 0,
    val pricePerSeat: Double,
    val punctualityRate: Int = 100,
    val isHabitRide: Boolean = false,
    val carModel: String = "",
    val licensePlate: String = "",
    val notes: String = "",
    val verifiedByUniversity: Boolean = true
)
