package com.wheels.app.core.analytics.bqt3.domain.repository

import com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage

interface BQT3Repository {
    suspend fun trackRidesNearMeUsage(userId: String): BQT3WeeklyUsage
    suspend fun getCurrentWeekUsage(userId: String): BQT3WeeklyUsage
    suspend fun syncPendingEvents()
}
