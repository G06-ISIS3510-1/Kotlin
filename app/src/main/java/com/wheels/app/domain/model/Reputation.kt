package com.wheels.app.domain.model

data class Reputation(
    val userId: String,
    val score: Double,
    val reviewsCount: Int,
    val badges: List<String>
)
