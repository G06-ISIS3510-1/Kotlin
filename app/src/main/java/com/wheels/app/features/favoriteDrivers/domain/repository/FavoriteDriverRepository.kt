package com.wheels.app.features.favoriteDrivers.domain.repository

import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import kotlinx.coroutines.flow.Flow

interface FavoriteDriverRepository {
    fun observeFavoriteDrivers(): Flow<List<FavoriteDriver>>
    fun observeFavoriteDriver(driverId: String): Flow<FavoriteDriver?>
    suspend fun favoriteDriver(driver: FavoriteDriver)
    suspend fun unfavoriteDriver(driverId: String)
    suspend fun syncPendingFavorites()
}
