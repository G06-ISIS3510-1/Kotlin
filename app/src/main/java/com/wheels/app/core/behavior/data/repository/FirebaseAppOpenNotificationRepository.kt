package com.wheels.app.core.behavior.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.repository.AppOpenNotificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseAppOpenNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : AppOpenNotificationRepository {

    override suspend fun prepareNotificationSignal(event: AppOpenedEvent) {
        withContext(ioDispatcher) {
            val document = firestore.collection(USER_USAGE_PATTERNS_COLLECTION)
                .document(event.uid)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(document)

                if (!snapshot.exists()) {
                    // Seed the aggregated profile with the full structure so the
                    // upcoming Cloud Function can update counts and prediction
                    // fields without having to backfill missing keys first.
                    transaction.set(
                        document,
                        mapOf(
                            "uid" to event.uid,
                            "email" to event.email,
                            "timezone" to event.timezone,
                            "hourCounts" to defaultHourCounts(),
                            "halfHourCounts" to defaultHalfHourCounts(),
                            "totalOpenCount" to 0,
                            "peakHour" to null,
                            "peakHalfHourBucket" to null,
                            "peakScore" to 0.0,
                            "lastOpenedAt" to event.openedAtMillis,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        document,
                        mapOf(
                            "email" to event.email,
                            "timezone" to event.timezone,
                            "lastOpenedAt" to event.openedAtMillis,
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                }
                null
            }
                .awaitResult()
        }
    }

    private fun defaultHourCounts(): Map<String, Int> {
        return (0..23).associate { hour -> hour.toString() to 0 }
    }

    private fun defaultHalfHourCounts(): Map<String, Int> {
        return (0..47).associate { bucket -> bucket.toString() to 0 }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        // Aggregated, per-user profile that the backend will enrich with counts
        // and predicted peak usage windows.
        const val USER_USAGE_PATTERNS_COLLECTION = "user_usage_patterns"
    }
}
