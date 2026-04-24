package com.wheels.app.core.analytics.bqt3.data.local

import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageEvent
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageSource
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class BQT3LocalDataSource @Inject constructor(
    private val usageDao: BQT3UsageDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun saveEvent(event: BQT3UsageEvent) = withContext(ioDispatcher) {
        usageDao.insertEvent(event.toEntity())
    }

    suspend fun getPendingEvents(): List<BQT3UsageEvent> = withContext(ioDispatcher) {
        usageDao.getPendingEvents().map { it.toDomain() }
    }

    suspend fun markEventsSynced(eventIds: List<String>) = withContext(ioDispatcher) {
        if (eventIds.isNotEmpty()) {
            usageDao.markEventsSynced(eventIds)
        }
    }

    suspend fun saveWeeklyUsage(summary: BQT3WeeklyUsage) = withContext(ioDispatcher) {
        usageDao.upsertWeeklyUsageCache(summary.toEntity())
    }

    suspend fun getCachedWeeklyUsage(
        userId: String,
        weekStartDate: String
    ): BQT3WeeklyUsage? = withContext(ioDispatcher) {
        usageDao.getWeeklyUsageCache(userId, weekStartDate)
            ?.toDomain(source = BQT3UsageSource.CACHE)
    }

    suspend fun calculateOfflineWeeklyUsage(
        userId: String,
        weekStartDate: String
    ): BQT3WeeklyUsage = withContext(ioDispatcher) {
        val cachedSummary = usageDao.getWeeklyUsageCache(userId, weekStartDate)
        val pendingCount = usageDao.countPendingEventsForWeek(userId, weekStartDate)
        val localCount = usageDao.countLocalEventsForWeek(userId, weekStartDate)
        val cachedCount = cachedSummary?.usageCount

        BQT3WeeklyUsage(
            userId = userId,
            weekStartDate = weekStartDate,
            usageCount = if (cachedCount == null) {
                localCount
            } else {
                cachedCount + pendingCount
            },
            lastUpdatedMillis = cachedSummary?.lastUpdatedMillis ?: System.currentTimeMillis(),
            source = BQT3UsageSource.CACHE
        )
    }
}
