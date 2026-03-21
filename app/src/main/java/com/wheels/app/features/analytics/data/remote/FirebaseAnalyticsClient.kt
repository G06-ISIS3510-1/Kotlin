package com.wheels.app.features.analytics.data.remote

import javax.inject.Inject

class FirebaseAnalyticsClient @Inject constructor() {
    suspend fun fetchUserRideHistory(userId: String): String {
        throw NotImplementedError("Pending Firebase analytics integration for user ride history.")
    }

    suspend fun fetchDestinationEvents(userId: String): String {
        throw NotImplementedError("Pending Firebase analytics integration for destination events.")
    }
}
