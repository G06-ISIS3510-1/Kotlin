package com.wheels.app.core.trust.domain.model

data class DriverTrustScore(
    val reliabilityScore: Int,
    val explanation: String,
    val updatedAtMillis: Long? = null
)
