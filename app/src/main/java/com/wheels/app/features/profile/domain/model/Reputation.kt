package com.wheels.app.features.profile.domain.model

data class Reputation(
    val userId: String,
    val score: Double,
    val reviewsCount: Int,
    val badges: List<String>
)
