package com.wheels.app.features.favoriteDrivers.domain.model

data class FavoriteDriver(
    val driverId: String,
    val driverName: String,
    val rating: Double,
    val trustScore: Double,
    val completedRides: Int,
    val profileImageUrl: String?,
    val savedAt: Long,
    val pendingSync: Boolean
)
