package com.wheels.app.features.favoriteDrivers.analytics.data.repository

import com.wheels.app.features.favoriteDrivers.analytics.data.remote.FavoriteDriverAnalyticsRemoteDataSource
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsEvent
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsSummary
import com.wheels.app.features.favoriteDrivers.analytics.domain.repository.FavoriteDriverAnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteDriverAnalyticsRepositoryImpl @Inject constructor(
    private val remoteDataSource: FavoriteDriverAnalyticsRemoteDataSource
) : FavoriteDriverAnalyticsRepository {

    override suspend fun trackEvent(event: FavoriteDriverAnalyticsEvent) {
        remoteDataSource.uploadEvent(event)
    }

    override suspend fun getMostFavoritedDrivers(): List<FavoriteDriverAnalyticsSummary> {
        return remoteDataSource.fetchMostFavoritedDrivers()
    }
}
