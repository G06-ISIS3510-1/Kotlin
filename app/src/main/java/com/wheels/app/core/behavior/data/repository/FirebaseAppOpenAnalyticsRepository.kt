package com.wheels.app.core.behavior.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.repository.AppOpenAnalyticsRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseAppOpenAnalyticsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : AppOpenAnalyticsRepository {

    override suspend fun recordAnalytics(event: AppOpenedEvent) {
        withContext(ioDispatcher) {
            val eventId = "analytics_app_open_${event.uid}_${event.openedAtMillis}_${UUID.randomUUID()}"
            firestore.collection(APP_OPEN_ANALYTICS_COLLECTION)
                .document(eventId)
                .set(
                    mapOf(
                        "eventType" to "app_opened",
                        "uid" to event.uid,
                        "email" to event.email,
                        "openedAt" to event.openedAtMillis,
                        "hourOfDay" to event.hourOfDay,
                        "minuteOfHour" to event.minuteOfHour,
                        "dayOfWeek" to event.dayOfWeek,
                        "timezone" to event.timezone,
                        "openSource" to event.openSource.storageValue,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val APP_OPEN_ANALYTICS_COLLECTION = "analytics_app_open_events"
    }
}
