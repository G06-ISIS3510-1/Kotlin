package com.wheels.app.core.analytics.bq13.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.wheels.app.core.analytics.bq13.domain.model.BQ13_CACHE_VERSION
import com.wheels.app.core.analytics.bq13.domain.model.BQ13DataSource
import com.wheels.app.core.analytics.bq13.domain.model.BQ13FrequentDestination
import com.wheels.app.core.analytics.bq13.domain.model.BQ13Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class BQ13RemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchFrequentDestinations(userId: String): BQ13Result = withContext(ioDispatcher) {
        val snapshot = firestore.collection(USER_DESTINATION_INSIGHTS_COLLECTION)
            .document(userId)
            .get()
            .awaitResult()

        snapshot.toBQ13Result(userId)
    }

    private fun DocumentSnapshot.toBQ13Result(userId: String): BQ13Result {
        val now = System.currentTimeMillis()
        val updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time
            ?: getTimestamp("lastUpdated")?.toDate()?.time
            ?: now

        @Suppress("UNCHECKED_CAST")
        val destinationMaps = (get("topDestinations") as? List<Map<String, Any?>>)
            ?: (get("destinationCounts") as? List<Map<String, Any?>>)
            ?: emptyList()

        val destinations = destinationMaps.mapNotNull { item ->
            val destinationName = item["destinationName"] as? String
                ?: item["name"] as? String
                ?: item["destination"] as? String
                ?: return@mapIndexedNotNull null

            val count = (item["bookingCount"] as? Number)?.toInt()
                ?: (item["count"] as? Number)?.toInt()
                ?: (item["frequency"] as? Number)?.toInt()
                ?: return@mapIndexedNotNull null

            val geoPoint = item["coordinates"] as? GeoPoint
                ?: item["destinationCoordinates"] as? GeoPoint

            BQ13FrequentDestination(
                userId = userId,
                destinationName = destinationName,
                latitude = (item["latitude"] as? Number)?.toDouble() ?: geoPoint?.latitude,
                longitude = (item["longitude"] as? Number)?.toDouble() ?: geoPoint?.longitude,
                count = count,
                lastUpdatedMillis = updatedAtMillis,
                cacheVersion = BQ13_CACHE_VERSION
            )
        }.sortedWith(
            compareByDescending<BQ13FrequentDestination> { it.count }
                .thenBy { it.destinationName }
        )

        return BQ13Result(
            userId = getString("userId").orEmpty().ifBlank { userId },
            destinations = destinations,
            lastUpdatedMillis = updatedAtMillis,
            cacheVersion = BQ13_CACHE_VERSION,
            source = BQ13DataSource.NETWORK
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val USER_DESTINATION_INSIGHTS_COLLECTION = "user_destination_insights"
    }
}
