package com.wheels.app.features.favoriteDrivers.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver

@Entity(tableName = "favorite_drivers")
data class FavoriteDriverEntity(
    @PrimaryKey val driverId: String,
    val driverName: String,
    val rating: Double,
    val trustScore: Double,
    val completedRides: Int,
    val profileImageUrl: String?,
    val savedAt: Long,
    val pendingSync: Boolean,
    val pendingDelete: Boolean = false
)

fun FavoriteDriverEntity.toDomain(): FavoriteDriver {
    return FavoriteDriver(
        driverId = driverId,
        driverName = driverName,
        rating = rating,
        trustScore = trustScore,
        completedRides = completedRides,
        profileImageUrl = profileImageUrl,
        savedAt = savedAt,
        pendingSync = pendingSync
    )
}

fun FavoriteDriver.toEntity(): FavoriteDriverEntity {
    return FavoriteDriverEntity(
        driverId = driverId,
        driverName = driverName,
        rating = rating,
        trustScore = trustScore,
        completedRides = completedRides,
        profileImageUrl = profileImageUrl,
        savedAt = savedAt,
        pendingSync = pendingSync
    )
}
