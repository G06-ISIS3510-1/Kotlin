package com.wheels.app.features.analytics.data.adapter

import com.wheels.app.features.analytics.data.remote.FirebaseAnalyticsClient
import com.wheels.app.features.analytics.domain.model.UserDestinationAnalytics
import com.wheels.app.features.analytics.domain.service.DestinationAnalyticsService
import javax.inject.Inject

class FirebaseAnalyticsAdapter @Inject constructor(
    private val firebaseAnalyticsClient: FirebaseAnalyticsClient
) : DestinationAnalyticsService {

    // Adapter keeps analytics logic isolated from Firebase-specific response details.
    override suspend fun getMostFrequentDestinations(userId: String): UserDestinationAnalytics {
        TODO("Pending Firebase response mapping and destination frequency computation.")
    }
}
