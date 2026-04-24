package com.wheels.app.core.analytics.bqt3.data.local

import androidx.room.Entity
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_CACHE_VERSION
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageSource
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage

@Entity(
    tableName = "bq_t3_weekly_usage_cache",
    primaryKeys = ["userId", "weekStartDate"]
)
data class BQT3WeeklyUsageEntity(
    val userId: String,
    val weekStartDate: String,
    val usageCount: Int,
    val lastUpdatedMillis: Long,
    val cacheVersion: String = BQT3_CACHE_VERSION
)

fun BQT3WeeklyUsageEntity.toDomain(source: BQT3UsageSource): BQT3WeeklyUsage {
    return BQT3WeeklyUsage(
        userId = userId,
        weekStartDate = weekStartDate,
        usageCount = usageCount,
        lastUpdatedMillis = lastUpdatedMillis,
        cacheVersion = cacheVersion,
        source = source
    )
}

fun BQT3WeeklyUsage.toEntity(): BQT3WeeklyUsageEntity {
    return BQT3WeeklyUsageEntity(
        userId = userId,
        weekStartDate = weekStartDate,
        usageCount = usageCount,
        lastUpdatedMillis = lastUpdatedMillis,
        cacheVersion = cacheVersion
    )
}
