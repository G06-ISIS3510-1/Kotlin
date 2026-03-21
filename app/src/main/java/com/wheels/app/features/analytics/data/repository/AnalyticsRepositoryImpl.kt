package com.wheels.app.features.analytics.data.repository

import com.wheels.app.features.analytics.domain.model.UserDestinationAnalytics
import com.wheels.app.features.analytics.domain.repository.AnalyticsRepository
import com.wheels.app.features.analytics.domain.service.DestinationAnalyticsService
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val destinationAnalyticsService: DestinationAnalyticsService
) : AnalyticsRepository {

    override suspend fun getUserDestinationAnalytics(userId: String): UserDestinationAnalytics {
        TODO("Pending repository orchestration for destination analytics retrieval.")
    }
}
