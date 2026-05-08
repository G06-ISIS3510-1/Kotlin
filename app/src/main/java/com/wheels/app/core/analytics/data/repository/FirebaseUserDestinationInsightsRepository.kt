package com.wheels.app.core.analytics.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.analytics.domain.model.DestinationInsight
import com.wheels.app.core.analytics.domain.model.UserDestinationInsights
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseUserDestinationInsightsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : UserDestinationInsightsRepository {

    override suspend fun logRideBookedDestination(
        userId: String,
        rideId: String,
        destinationName: String
    ) {
        withContext(ioDispatcher) {
            val firestore = requireFirestore()
            val normalizedDestination = normalizeDestination(destinationName)
            val eventId = "ride_booked_${userId}_${rideId}_${UUID.randomUUID()}"

            firestore.collection(DESTINATION_EVENTS_COLLECTION)
                .document(eventId)
                .set(
                    mapOf(
                        "eventType" to "ride_booked",
                        "userId" to userId,
                        "rideId" to rideId,
                        "destinationName" to destinationName,
                        "destinationKey" to normalizedDestination,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
        }
    }

    override fun observeUserDestinationInsights(userId: String): Flow<UserDestinationInsights?> {
        val firestore = getFirestoreOrNull() ?: return flowOf(null)

        return callbackFlow {
            val registration = firestore
                .collection(USER_DESTINATION_INSIGHTS_COLLECTION)
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val insights = if (snapshot == null || !snapshot.exists()) {
                        null
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val topDestinations = (snapshot.get("topDestinations") as? List<Map<String, Any?>>)
                            .orEmpty()
                            .mapIndexedNotNull { index, item ->
                                val destinationName = item["destinationName"] as? String ?: return@mapIndexedNotNull null
                                val bookingCount = (item["bookingCount"] as? Number)?.toInt() ?: return@mapIndexedNotNull null
                                DestinationInsight(
                                    destinationName = destinationName,
                                    bookingCount = bookingCount,
                                    rank = (item["rank"] as? Number)?.toInt() ?: (index + 1)
                                )
                            }

                        UserDestinationInsights(
                            userId = snapshot.getString("userId").orEmpty().ifBlank { userId },
                            topDestinations = topDestinations,
                            totalBookingsTracked = snapshot.getLong("totalBookingsTracked")?.toInt() ?: 0,
                            lastUpdatedMillis = snapshot.getTimestamp("updatedAt")?.toDate()?.time
                                ?: snapshot.getLong("lastUpdatedMillis")
                        )
                    }

                    trySend(insights)
                }

            awaitClose { registration.remove() }
        }.flowOn(ioDispatcher)
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? {
        val app = FirebaseApp.getApps(context).firstOrNull() ?: return null
        return FirebaseFirestore.getInstance(app)
    }

    private fun requireFirestore(): FirebaseFirestore {
        return getFirestoreOrNull()
            ?: throw IllegalStateException(
                "Firebase is not configured yet. Add app/google-services.json to enable destination insights."
            )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private fun normalizeDestination(value: String): String {
        return value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), "_")
    }

    private companion object {
        const val DESTINATION_EVENTS_COLLECTION = "analytics_destination_events"
        const val USER_DESTINATION_INSIGHTS_COLLECTION = "user_destination_insights"
    }
}
