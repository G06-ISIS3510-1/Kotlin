package com.wheels.app.core.analytics.domain.repository

import com.wheels.app.core.analytics.domain.model.UserDestinationInsights
import kotlinx.coroutines.flow.Flow

interface UserDestinationInsightsRepository {
    suspend fun logRideBookedDestination(
        userId: String,
        rideId: String,
        destinationName: String
    )

    fun observeUserDestinationInsights(userId: String): Flow<UserDestinationInsights?>
}
