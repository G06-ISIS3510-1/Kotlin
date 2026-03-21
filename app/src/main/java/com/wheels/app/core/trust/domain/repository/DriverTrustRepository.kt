package com.wheels.app.core.trust.domain.repository

import com.wheels.app.core.trust.domain.model.DriverTrustScore
import com.wheels.app.core.trust.domain.model.TrustScoreNotice
import kotlinx.coroutines.flow.Flow

interface DriverTrustRepository {
    fun observeDriverTrustScore(userId: String): Flow<DriverTrustScore?>

    suspend fun startRide(params: DriverRideTrustActionParams)

    suspend fun completeRideAndAwaitTrustUpdate(
        params: DriverRideTrustActionParams
    ): TrustScoreNotice

    suspend fun cancelRideAndAwaitTrustUpdate(
        params: DriverRideTrustActionParams
    ): TrustScoreNotice
}

data class DriverRideTrustActionParams(
    val rideId: String,
    val driverId: String,
    val scheduledStartAtMillis: Long
)
