package com.wheels.app.core.analytics.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.analytics.domain.model.RoleChangeEvent
import com.wheels.app.core.analytics.domain.repository.RoleChangeEventRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseRoleChangeEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) : RoleChangeEventRepository {

    override suspend fun trackRoleChange(event: RoleChangeEvent) {
        withContext(ioDispatcher) {
            val eventId = buildEventId(event)
            // Append-only event collection. This is ideal for BigQuery streaming
            // and Looker Studio dashboards because each role change becomes one row.
            firestore.collection(ROLE_CHANGE_EVENTS_COLLECTION)
                .document(eventId)
                .set(
                    mapOf(
                        "uid" to event.uid,
                        "email" to event.email,
                        "oldRole" to event.oldRole,
                        "newRole" to event.newRole,
                        "sourceScreen" to event.sourceScreen,
                        "sourceAction" to event.sourceAction,
                        "changedAt" to event.changedAtMillis,
                        "changedHour" to event.changedHour,
                        "changedMinute" to event.changedMinute,
                        "changedMonthKey" to event.changedMonthKey,
                        "timezone" to event.timezone,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
        }
    }

    private fun buildEventId(event: RoleChangeEvent): String {
        return "role_change_${event.uid}_${event.changedAtMillis}_${UUID.randomUUID()}"
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val ROLE_CHANGE_EVENTS_COLLECTION = "role_change_events"
    }
}
