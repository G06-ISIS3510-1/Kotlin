package com.wheels.app.core.analytics.bqt3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_CACHE_VERSION
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_FEATURE_NAME
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageEvent

@Entity(tableName = "bq_t3_usage_events")
data class BQT3UsageEventEntity(
    @PrimaryKey val eventId: String,
    val userId: String,
    val timestampMillis: Long,
    val weekStartDate: String,
    val featureName: String = BQT3_FEATURE_NAME,
    val synced: Boolean = false,
    val cacheVersion: String = BQT3_CACHE_VERSION
)

fun BQT3UsageEventEntity.toDomain(): BQT3UsageEvent {
    return BQT3UsageEvent(
        eventId = eventId,
        userId = userId,
        timestampMillis = timestampMillis,
        weekStartDate = weekStartDate,
        featureName = featureName,
        synced = synced,
        cacheVersion = cacheVersion
    )
}

fun BQT3UsageEvent.toEntity(): BQT3UsageEventEntity {
    return BQT3UsageEventEntity(
        eventId = eventId,
        userId = userId,
        timestampMillis = timestampMillis,
        weekStartDate = weekStartDate,
        featureName = featureName,
        synced = synced,
        cacheVersion = cacheVersion
    )
}
