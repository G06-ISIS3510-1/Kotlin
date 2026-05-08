package com.wheels.app.features.rides.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RideOfflineDao {

    @Query("SELECT * FROM create_ride_drafts WHERE driverId = :driverId LIMIT 1")
    fun observeCreateRideDraft(driverId: String): Flow<CreateRideDraftEntity?>

    @Upsert
    suspend fun upsertCreateRideDraft(draft: CreateRideDraftEntity)

    @Query("DELETE FROM create_ride_drafts WHERE driverId = :driverId")
    suspend fun clearCreateRideDraft(driverId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRidePublish(entity: PendingRidePublishEntity)

    @Query("SELECT * FROM pending_ride_publishes WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    fun observePendingRidePublishes(driverId: String): Flow<List<PendingRidePublishEntity>>

    @Query("SELECT * FROM pending_ride_publishes WHERE driverId = :driverId ORDER BY createdAtMillis ASC")
    suspend fun getPendingRidePublishes(driverId: String): List<PendingRidePublishEntity>

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
