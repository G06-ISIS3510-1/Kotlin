package com.wheels.app.core.analytics.bqt3.domain.model

data class BQT3UsageEvent(
    val eventId: String,
    val userId: String,
    val timestampMillis: Long,
    val weekStartDate: String,
    val featureName: String = BQT3_FEATURE_NAME,
    val synced: Boolean = false,
    val cacheVersion: String = BQT3_CACHE_VERSION
)

data class BQT3WeeklyUsage(
    val userId: String,
    val weekStartDate: String,
    val usageCount: Int,
    val lastUpdatedMillis: Long,
    val cacheVersion: String = BQT3_CACHE_VERSION,
    val source: BQT3UsageSource
)

enum class BQT3UsageSource {
    NETWORK,
    CACHE
}

const val BQT3_FEATURE_NAME = "rides_near_me"
const val BQT3_CACHE_VERSION = "bq_t3_usage_events_v1"
