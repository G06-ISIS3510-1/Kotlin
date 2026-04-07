package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class PublishRideRequest(
    val driverId: String,
    val driverName: String,
    val driverEmail: String,
    val origin: String,
    val originSearch: String,
    val destination: String,
    val destinationSearch: String,
    val departureAt: Instant,
    val estimatedDurationMinutes: Int,
    val totalSeats: Int,
    val pricePerSeat: Int,
    val carModel: String,
    val licensePlate: String,
    val notes: String,
    val driverRating: Int,
    val onTimeRate: Int,
    val reviewCount: Int,
    val verifiedByUniversity: Boolean,
    val passengerIds: List<String> = emptyList()
)
