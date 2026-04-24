package com.wheels.app.core.analytics.bqt3.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_CACHE_VERSION
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageEvent
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageSource
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class BQT3RemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun uploadEvent(event: BQT3UsageEvent) = withContext(ioDispatcher) {
        firestore.runTransaction { transaction ->
            val eventRef = firestore.collection(EVENTS_COLLECTION).document(event.eventId)
            val summaryRef = firestore.collection(WEEKLY_USAGE_COLLECTION)
                .document(summaryDocumentId(event.userId, event.weekStartDate))
            val eventSnapshot = transaction.get(eventRef)

            if (!eventSnapshot.exists()) {
                transaction.set(
                    eventRef,
                    mapOf(
                        "eventId" to event.eventId,
                        "userId" to event.userId,
                        "timestamp" to Timestamp(event.timestampMillis / 1000, 0),
                        "timestampMillis" to event.timestampMillis,
                        "weekStartDate" to event.weekStartDate,
                        "featureName" to event.featureName,
                        "synced" to true,
                        "cacheVersion" to BQT3_CACHE_VERSION,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    summaryRef,
                    mapOf(
                        "userId" to event.userId,
                        "weekStartDate" to event.weekStartDate,
                        "featureName" to event.featureName,
                        "usageCount" to FieldValue.increment(1),
                        "cacheVersion" to BQT3_CACHE_VERSION,
                        "lastUpdatedAt" to FieldValue.serverTimestamp(),
                        "lastUpdatedMillis" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }
            null
        }.awaitResult()
    }

    suspend fun fetchWeeklyUsage(
        userId: String,
        weekStartDate: String
    ): BQT3WeeklyUsage = withContext(ioDispatcher) {
        val snapshot = firestore.collection(WEEKLY_USAGE_COLLECTION)
            .document(summaryDocumentId(userId, weekStartDate))
            .get()
            .awaitResult()

        snapshot.toWeeklyUsage(userId, weekStartDate)
    }

    private fun DocumentSnapshot.toWeeklyUsage(
        userId: String,
        weekStartDate: String
    ): BQT3WeeklyUsage {
        return BQT3WeeklyUsage(
            userId = getString("userId").orEmpty().ifBlank { userId },
            weekStartDate = getString("weekStartDate").orEmpty().ifBlank { weekStartDate },
            usageCount = getLong("usageCount")?.toInt() ?: 0,
            lastUpdatedMillis = getLong("lastUpdatedMillis")
                ?: getTimestamp("lastUpdatedAt")?.toDate()?.time
                ?: System.currentTimeMillis(),
            cacheVersion = getString("cacheVersion") ?: BQT3_CACHE_VERSION,
            source = BQT3UsageSource.NETWORK
        )
    }

    private fun summaryDocumentId(userId: String, weekStartDate: String): String {
        return "${userId}_$weekStartDate"
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val EVENTS_COLLECTION = "bq_t3_usage_events_v1"
        const val WEEKLY_USAGE_COLLECTION = "bq_t3_weekly_usage_v1"
    }
}
