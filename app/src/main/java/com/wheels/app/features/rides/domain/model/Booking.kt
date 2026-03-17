package com.wheels.app.features.rides.domain.model

data class Booking(
    val id: String,
    val rideId: String,
    val passengerId: String,
    val seatsReserved: Int,
    val status: String
)
