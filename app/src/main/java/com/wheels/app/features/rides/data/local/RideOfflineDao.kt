package com.wheels.app.features.rides.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RideOfflineDao {

    @Query("SELECT * FROM near_rides_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getNearRidesCache(cacheKey: String): NearRidesCacheEntity?

    @Query("SELECT * FROM near_rides_cache ORDER BY cachedAtMillis DESC LIMIT 1")
    suspend fun getLatestNearRidesCache(): NearRidesCacheEntity?

    @Upsert
    suspend fun upsertNearRidesCache(entity: NearRidesCacheEntity)

    @Query("DELETE FROM near_rides_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteNearRidesCache(cacheKey: String)

    @Query("SELECT * FROM create_ride_drafts WHERE driverId = :driverId ORDER BY updatedAtMillis DESC LIMIT 1")
    fun observeCreateRideDraft(driverId: String): Flow<CreateRideDraftEntity?>

    @Query("SELECT * FROM create_ride_drafts WHERE driverId = :driverId ORDER BY updatedAtMillis DESC")
    fun observeCreateRideDrafts(driverId: String): Flow<List<CreateRideDraftEntity>>

    @Query("SELECT * FROM create_ride_drafts WHERE draftId = :draftId LIMIT 1")
    suspend fun getCreateRideDraft(draftId: String): CreateRideDraftEntity?

    @Upsert
    suspend fun upsertCreateRideDraft(draft: CreateRideDraftEntity)

    @Query("DELETE FROM create_ride_drafts WHERE draftId = :draftId")
    suspend fun deleteCreateRideDraft(draftId: String)

    @Query("DELETE FROM create_ride_drafts WHERE driverId = :driverId")
    suspend fun clearCreateRideDraft(driverId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRidePublish(entity: PendingRidePublishEntity)

    @Query("SELECT * FROM pending_ride_publishes WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    fun observePendingRidePublishes(driverId: String): Flow<List<PendingRidePublishEntity>>

    @Query("SELECT * FROM pending_ride_publishes WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    suspend fun getPendingRidePublishes(driverId: String): List<PendingRidePublishEntity>

    @Query("SELECT * FROM pending_ride_publishes WHERE id = :id LIMIT 1")
    suspend fun getPendingRidePublish(id: String): PendingRidePublishEntity?

    @Query("DELETE FROM pending_ride_publishes WHERE id = :id")
    suspend fun deletePendingRidePublish(id: String)

    @Query(
        """
        UPDATE pending_ride_publishes
        SET retryCount = retryCount + 1,
            lastError = :lastError
        WHERE id = :id
        """
    )
    suspend fun markPendingRidePublishFailed(id: String, lastError: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRideAction(entity: PendingRideActionEntity)

    @Query("SELECT * FROM pending_ride_actions WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    fun observePendingRideActions(driverId: String): Flow<List<PendingRideActionEntity>>

    @Query("SELECT * FROM pending_ride_actions WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    suspend fun getPendingRideActions(driverId: String): List<PendingRideActionEntity>

    @Query("DELETE FROM pending_ride_actions WHERE rideId = :rideId")
    suspend fun deletePendingRideAction(rideId: String)

    @Query(
        """
        UPDATE pending_ride_actions
        SET retryCount = retryCount + 1,
            lastError = :lastError
        WHERE rideId = :rideId
        """
    )
    suspend fun markPendingRideActionFailed(rideId: String, lastError: String)
}
