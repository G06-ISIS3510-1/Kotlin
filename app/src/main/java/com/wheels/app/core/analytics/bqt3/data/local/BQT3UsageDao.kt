package com.wheels.app.core.analytics.bqt3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_CACHE_VERSION
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_FEATURE_NAME

@Dao
interface BQT3UsageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: BQT3UsageEventEntity)

    @Query(
        """
        SELECT * FROM bq_t3_usage_events
        WHERE synced = 0 AND cacheVersion = :cacheVersion
        ORDER BY timestampMillis ASC
        """
    )
    suspend fun getPendingEvents(
        cacheVersion: String = BQT3_CACHE_VERSION
    ): List<BQT3UsageEventEntity>

    @Query("UPDATE bq_t3_usage_events SET synced = 1 WHERE eventId IN (:eventIds)")
    suspend fun markEventsSynced(eventIds: List<String>)

    @Query(
        """
        SELECT COUNT(*) FROM bq_t3_usage_events
        WHERE userId = :userId
        AND weekStartDate = :weekStartDate
        AND featureName = :featureName
        AND cacheVersion = :cacheVersion
        """
    )
    suspend fun countLocalEventsForWeek(
        userId: String,
        weekStartDate: String,
        featureName: String = BQT3_FEATURE_NAME,
        cacheVersion: String = BQT3_CACHE_VERSION
    ): Int

    @Query(
        """
        SELECT COUNT(*) FROM bq_t3_usage_events
        WHERE userId = :userId
        AND weekStartDate = :weekStartDate
        AND featureName = :featureName
        AND synced = 0
        AND cacheVersion = :cacheVersion
        """
    )
    suspend fun countPendingEventsForWeek(
        userId: String,
        weekStartDate: String,
        featureName: String = BQT3_FEATURE_NAME,
        cacheVersion: String = BQT3_CACHE_VERSION
    ): Int

    @Query(
        """
        SELECT * FROM bq_t3_weekly_usage_cache
        WHERE userId = :userId
        AND weekStartDate = :weekStartDate
        AND cacheVersion = :cacheVersion
        LIMIT 1
        """
    )
    suspend fun getWeeklyUsageCache(
        userId: String,
        weekStartDate: String,
        cacheVersion: String = BQT3_CACHE_VERSION
    ): BQT3WeeklyUsageEntity?

    @Upsert
    suspend fun upsertWeeklyUsageCache(summary: BQT3WeeklyUsageEntity)
}
