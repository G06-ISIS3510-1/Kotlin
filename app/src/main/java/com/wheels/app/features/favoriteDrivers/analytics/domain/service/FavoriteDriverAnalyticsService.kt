package com.wheels.app.features.favoriteDrivers.analytics.domain.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsEvent
import com.wheels.app.features.favoriteDrivers.analytics.domain.repository.FavoriteDriverAnalyticsRepository
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class FavoriteDriverAnalyticsService @Inject constructor(
    private val repository: FavoriteDriverAnalyticsRepository,
    private val firebaseAuth: FirebaseAuth,
    ioDispatcher: CoroutineDispatcher
) {
    private val analyticsScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun trackFavoriteAdded(driver: FavoriteDriver) {
        track(driver, FavoriteDriverAnalyticsEvent.FAVORITE_ADDED)
    }

    fun trackFavoriteRemoved(driver: FavoriteDriver) {
        track(driver, FavoriteDriverAnalyticsEvent.FAVORITE_REMOVED)
    }

    private fun track(driver: FavoriteDriver, eventType: String) {
        analyticsScope.launch {
            runCatching {
                repository.trackEvent(
                    FavoriteDriverAnalyticsEvent(
                        eventId = "favorite_driver_${System.currentTimeMillis()}_${UUID.randomUUID()}",
                        passengerId = firebaseAuth.currentUser?.uid ?: LOCAL_PASSENGER_ID,
                        driverId = driver.driverId,
                        driverName = driver.driverName,
                        eventType = eventType,
                        trustScore = driver.trustScore,
                        rating = driver.rating
                    )
                )
            }.onFailure { throwable ->
                Log.w(TAG, "Favorite driver analytics upload failed.", throwable)
            }
        }
    }

    companion object {
        private const val TAG = "FavoriteDriverAnalytics"
        private const val LOCAL_PASSENGER_ID = "local_passenger"
    }
}
