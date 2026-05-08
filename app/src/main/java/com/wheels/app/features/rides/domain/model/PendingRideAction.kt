package com.wheels.app.features.rides.domain.model

data class PendingRideAction(
    val rideId: String,
    val driverId: String,
    val actionType: PendingRideActionType,
    val scheduledStartAtMillis: Long,
    val previousTrustScore: Int?,
    val retryCount: Int,
    val lastError: String?,
    val createdAtMillis: Long
)

enum class PendingRideActionType {
    START,
    COMPLETE,
    CANCEL,
    DELETE
}

data class PendingRideActionSyncResult(
    val rideId: String,
    val actionType: PendingRideActionType,
    val previousTrustScore: Int?,
    val newTrustScore: Int?
)
