package com.wheels.app.features.favoriteDrivers.analytics.domain.model

data class FavoriteDriverAnalyticsEvent(
    val eventId: String,
    val passengerId: String,
    val driverId: String,
    val driverName: String,
    val eventType: String,
    val trustScore: Double,
    val rating: Double
) {
    companion object {
        const val FAVORITE_ADDED = "favorite_added"
        const val FAVORITE_REMOVED = "favorite_removed"
    }
}

data class FavoriteDriverAnalyticsSummary(
    val driverId: String,
    val driverName: String,
    val favoriteCount: Int
)
