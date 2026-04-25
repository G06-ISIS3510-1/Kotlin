package com.wheels.app.core.analytics.bq13.domain.model

data class BQ13FrequentDestination(
    val userId: String,
    val destinationName: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val count: Int,
    val lastUpdatedMillis: Long,
    val cacheVersion: String = BQ13_CACHE_VERSION
)

data class BQ13Result(
    val userId: String,
    val destinations: List<BQ13FrequentDestination>,
    val lastUpdatedMillis: Long,
    val cacheVersion: String = BQ13_CACHE_VERSION,
    val source: BQ13DataSource
)

enum class BQ13DataSource {
    NETWORK,
    CACHE
}

const val BQ13_CACHE_VERSION = "bq13_cache_v1"
