package com.wheels.app.features.analytics.domain.repository

import com.wheels.app.features.analytics.domain.model.UserDestinationAnalytics

interface AnalyticsRepository {
    suspend fun getUserDestinationAnalytics(userId: String): UserDestinationAnalytics
}
