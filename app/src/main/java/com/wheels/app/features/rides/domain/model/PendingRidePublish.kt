package com.wheels.app.features.rides.domain.model

data class PendingRidePublish(
    val id: String,
    val request: PublishRideRequest,
    val retryCount: Int,
    val lastError: String?,
    val createdAtMillis: Long
)
