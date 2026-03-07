package com.wheels.app.domain.model

data class Payment(
    val id: String,
    val bookingId: String,
    val amount: Double,
    val currency: String,
    val status: String
)
