package com.wheels.app.features.rides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.rides.domain.model.PendingRideAction
import com.wheels.app.features.rides.domain.model.PendingRideActionType

@Entity(tableName = "pending_ride_actions")
data class PendingRideActionEntity(
    @PrimaryKey val rideId: String,
    val driverId: String,
    val actionType: String,
    val scheduledStartAtMillis: Long,
    val previousTrustScore: Int?,
    val retryCount: Int,
    val lastError: String?,
    val createdAtMillis: Long
)

fun PendingRideAction.toEntity(): PendingRideActionEntity {
    return PendingRideActionEntity(
        rideId = rideId,
        driverId = driverId,
        actionType = actionType.name,
        scheduledStartAtMillis = scheduledStartAtMillis,
        previousTrustScore = previousTrustScore,
        retryCount = retryCount,
        lastError = lastError,
        createdAtMillis = createdAtMillis
    )
}

fun PendingRideActionEntity.toDomain(): PendingRideAction {
    return PendingRideAction(
        rideId = rideId,
        driverId = driverId,
        actionType = PendingRideActionType.valueOf(actionType),
        scheduledStartAtMillis = scheduledStartAtMillis,
        previousTrustScore = previousTrustScore,
        retryCount = retryCount,
        lastError = lastError,
        createdAtMillis = createdAtMillis
    )
}
