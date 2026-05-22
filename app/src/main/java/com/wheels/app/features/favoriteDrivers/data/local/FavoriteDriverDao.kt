package com.wheels.app.features.favoriteDrivers.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDriverDao {
    @Query("SELECT * FROM favorite_drivers WHERE pendingDelete = 0 ORDER BY savedAt DESC")
    fun observeFavoriteDrivers(): Flow<List<FavoriteDriverEntity>>

    @Query("SELECT * FROM favorite_drivers WHERE driverId = :driverId AND pendingDelete = 0 LIMIT 1")
    fun observeFavoriteDriver(driverId: String): Flow<FavoriteDriverEntity?>

    @Query("SELECT * FROM favorite_drivers WHERE driverId = :driverId LIMIT 1")
    suspend fun getFavoriteDriver(driverId: String): FavoriteDriverEntity?

    @Query("SELECT * FROM favorite_drivers WHERE pendingSync = 1")
    suspend fun getPendingSyncDrivers(): List<FavoriteDriverEntity>

    @Upsert
    suspend fun upsertFavoriteDriver(driver: FavoriteDriverEntity)

    @Query("UPDATE favorite_drivers SET pendingSync = 0 WHERE driverId = :driverId")
    suspend fun markSynced(driverId: String)

    @Query("UPDATE favorite_drivers SET pendingSync = 1, pendingDelete = 1 WHERE driverId = :driverId")
    suspend fun markPendingDelete(driverId: String)

    @Query("DELETE FROM favorite_drivers WHERE driverId = :driverId")
    suspend fun deleteFavoriteDriver(driverId: String)
}
