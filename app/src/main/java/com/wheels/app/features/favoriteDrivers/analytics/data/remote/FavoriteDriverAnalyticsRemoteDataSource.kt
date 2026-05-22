package com.wheels.app.features.favoriteDrivers.analytics.data.remote

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsEvent
import com.wheels.app.features.favoriteDrivers.analytics.domain.model.FavoriteDriverAnalyticsSummary
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FavoriteDriverAnalyticsRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun uploadEvent(event: FavoriteDriverAnalyticsEvent) = withContext(ioDispatcher) {
        val data = mapOf(
            "eventId" to event.eventId,
            "passengerId" to event.passengerId,
            "driverId" to event.driverId,
            "driverName" to event.driverName,
            "eventType" to event.eventType,
            "timestamp" to FieldValue.serverTimestamp(),
            "trustScore" to event.trustScore,
            "rating" to event.rating
        )

        Tasks.await(
            firestore
                .collection(FAVORITE_DRIVER_EVENTS_COLLECTION)
                .document(event.eventId)
                .set(data)
        )
    }

    suspend fun fetchMostFavoritedDrivers(): List<FavoriteDriverAnalyticsSummary> = withContext(ioDispatcher) {
        runCatching {
            val snapshot = Tasks.await(
                firestore
                    .collection(FAVORITE_DRIVER_EVENTS_COLLECTION)
                    .whereEqualTo("eventType", FavoriteDriverAnalyticsEvent.FAVORITE_ADDED)
                    .get()
            )

            snapshot.documents
                .mapNotNull { document ->
                    val driverId = document.getString("driverId") ?: return@mapNotNull null
                    val driverName = document.getString("driverName").orEmpty().ifBlank { "Unknown driver" }
                    driverId to driverName
                }
                .groupBy { it.first }
                .map { (driverId, events) ->
                    FavoriteDriverAnalyticsSummary(
                        driverId = driverId,
                        driverName = events.first().second,
                        favoriteCount = events.size
                    )
                }
                .sortedByDescending { it.favoriteCount }
                .take(MAX_RANKING_ITEMS)
        }.onFailure { throwable ->
            Log.w(TAG, "Could not load favorite driver analytics.", throwable)
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val TAG = "FavoriteDriverAnalytics"
        private const val FAVORITE_DRIVER_EVENTS_COLLECTION = "favorite_driver_events"
        private const val MAX_RANKING_ITEMS = 5
    }
}
