package com.wheels.app.features.rides.domain.model

data class CancellationBehaviorMetrics(
    val userId: String,
    val cancellationCount: Int,
    val averageHoursBeforeCancellation: Double
)
