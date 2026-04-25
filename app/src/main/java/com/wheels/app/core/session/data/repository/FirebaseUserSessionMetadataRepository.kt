package com.wheels.app.core.session.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.wheels.app.core.session.domain.repository.UserSessionMetadataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class FirebaseUserSessionMetadataRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val ioDispatcher: CoroutineDispatcher
) : UserSessionMetadataRepository {

    override suspend fun syncCurrentSessionMetadata() {
        withContext(ioDispatcher) {
            val currentUser = firebaseAuth.currentUser ?: return@withContext
            val token = runCatching { firebaseMessaging.token.awaitResult() }.getOrNull()
            val updates = mutableMapOf<String, Any>(
                "lastSeenAt" to FieldValue.serverTimestamp()
            )

            if (!token.isNullOrBlank()) {
                updates["fcmTokens"] = FieldValue.arrayUnion(token)
                updates["lastTokenUpdatedAt"] = FieldValue.serverTimestamp()
            }

            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .awaitResult()
        }
    }

    override suspend fun updateCurrentUserFcmToken(token: String) {
        withContext(ioDispatcher) {
            if (token.isBlank()) return@withContext
            val currentUser = firebaseAuth.currentUser ?: return@withContext
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .set(
                    mapOf(
                        "fcmTokens" to FieldValue.arrayUnion(token),
                        "lastTokenUpdatedAt" to FieldValue.serverTimestamp(),
                        "lastSeenAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
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
        const val USERS_COLLECTION = "users"
    }
}
