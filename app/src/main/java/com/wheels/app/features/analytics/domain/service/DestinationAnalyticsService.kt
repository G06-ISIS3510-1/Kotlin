package com.wheels.app.features.analytics.domain.service

import com.wheels.app.features.analytics.domain.model.UserDestinationAnalytics

interface DestinationAnalyticsService {
    suspend fun getMostFrequentDestinations(userId: String): UserDestinationAnalytics
}
