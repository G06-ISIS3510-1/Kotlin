package com.wheels.app.core.behavior.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.repository.AppOpenTrackingRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseAppOpenTrackingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : AppOpenTrackingRepository {

    override suspend fun recordAppOpened(event: AppOpenedEvent) {
        withContext(ioDispatcher) {
            val eventId = "app_open_${event.uid}_${event.openedAtMillis}_${UUID.randomUUID()}"
            // This collection is the raw event layer: each open is stored as an
            // immutable observation that backend code can aggregate later.
            firestore.collection(APP_OPEN_EVENTS_COLLECTION)
                .document(eventId)
                .set(
                    mapOf(
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
        // Raw app-open stream used for analytics and later aggregation.
        const val APP_OPEN_EVENTS_COLLECTION = "app_open_events"
    }
}
