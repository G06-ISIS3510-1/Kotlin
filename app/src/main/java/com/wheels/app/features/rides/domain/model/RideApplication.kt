package com.wheels.app.features.rides.domain.model

import java.time.Instant

data class RideApplication(
    val id: String,
    val rideId: String,
    val passengerId: String,
    val passengerName: String,
    val passengerEmail: String,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val isPaymentLocked: Boolean,
    val paymentStatusSource: String,
    val statusDetail: String? = null,
    val appliedAt: Instant? = null,
    val updatedAt: Instant? = null
)
