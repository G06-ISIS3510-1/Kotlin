package com.wheels.app.features.payments.data.remote.dto

data class PaymentDto(
    val id: String,
    val bookingId: String,
    val amount: Double,
    val currency: String,
    val status: String
)
