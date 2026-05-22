package com.wheels.app.features.favoriteDrivers.analytics.domain.repository

import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsEvent
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsSummary

interface FavoriteDriverAnalyticsRepository {
    suspend fun trackEvent(event: FavoriteDriverAnalyticsEvent)
    suspend fun getMostFavoritedDrivers(): List<FavoriteDriverAnalyticsSummary>
}
