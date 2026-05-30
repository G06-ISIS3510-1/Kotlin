package com.wheels.app.features.messages.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.core.session.UserRole
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun observeInbox(
        userId: String,
        activeRole: UserRole
    ): Flow<List<RideChatRemoteThread>> {
        if (userId.isBlank()) {
            return flowOf(emptyList())
        }

        return callbackFlow {
            val query = when (activeRole) {
                UserRole.PASSENGER -> firestore.collection(RIDES_COLLECTION)
                    .whereArrayContains(PASSENGER_IDS_FIELD, userId)
                UserRole.DRIVER -> firestore.collection(RIDES_COLLECTION)
                    .whereEqualTo(DRIVER_ID_FIELD, userId)
            }

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    try {
                        val threads = mutableListOf<RideChatRemoteThread>()
                        for (document in snapshot?.documents.orEmpty()) {
                        val thread = parseRideThread(document)
                            if (thread != null && thread.messages.isNotEmpty()) {
                                threads += thread
                            }
                        }
                        trySend(threads)
                    } catch (_: Throwable) {
                        // Keep the flow alive and let the local cache continue rendering.
                    }
                }
            }

            awaitClose { registration.remove() }
        }
    }

    fun observeRideChat(rideId: String): Flow<RideChatRemoteThread?> {
        if (rideId.isBlank()) {
            return flowOf(null)
        }

        return callbackFlow {
            val registration = firestore.collection(RIDES_COLLECTION)
                .document(rideId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    launch {
                        try {
                            val thread = if (snapshot == null) null else parseRideThread(snapshot)
                            trySend(thread)
                        } catch (_: Throwable) {
                            // Ignore malformed snapshots and wait for the next update.
                        }
                    }
                }

            awaitClose { registration.remove() }
        }
    }

    suspend fun fetchRideChat(rideId: String): RideChatRemoteThread? = withContext(ioDispatcher) {
        if (rideId.isBlank()) return@withContext null
        val snapshot = firestore.collection(RIDES_COLLECTION)
            .document(rideId)
            .get()
            .awaitResult()
        if (!snapshot.exists()) return@withContext null
        parseRideThread(snapshot)
    }

    suspend fun sendMessage(
        rideId: String,
        message: RideChatRemoteMessage,
        activeRole: UserRole
    ) = withContext(ioDispatcher) {
        val currentThread = fetchRideChat(rideId)
            ?: throw IllegalStateException("Ride not found.")

        val senderIsDriver = message.senderId == currentThread.driverId
        val senderIsPassenger = message.senderId == currentThread.passengerId
        if (!senderIsDriver && !senderIsPassenger) {
            throw IllegalStateException("The message sender is not part of this ride.")
        }

        val updates = mutableMapOf<String, Any>(
            CHAT_MESSAGES_FIELD to FieldValue.arrayUnion(message.toFirestoreMap()),
            CHAT_HAS_MESSAGES_FIELD to true,
            CHAT_LAST_MESSAGE_FIELD to message.content,
            CHAT_LAST_MESSAGE_AT_FIELD to message.sentAtMillis,
            CHAT_LAST_SENDER_ID_FIELD to message.senderId,
            CHAT_UPDATED_AT_FIELD to message.sentAtMillis
        )

        when (activeRole) {
            UserRole.PASSENGER -> {
                updates[CHAT_PASSENGER_READ_AT_FIELD] = message.sentAtMillis
                updates[CHAT_PASSENGER_NAME_FIELD] = message.senderName
                updates[CHAT_DRIVER_NAME_FIELD] = currentThread.driverName
            }
            UserRole.DRIVER -> {
                updates[CHAT_DRIVER_READ_AT_FIELD] = message.sentAtMillis
                updates[CHAT_DRIVER_NAME_FIELD] = message.senderName
                updates[CHAT_PASSENGER_NAME_FIELD] = currentThread.passengerName
            }
        }

        firestore.collection(RIDES_COLLECTION)
            .document(rideId)
            .update(updates)
            .awaitResult()
    }

    suspend fun markConversationRead(
        rideId: String,
        activeRole: UserRole,
        readAtMillis: Long
    ) = withContext(ioDispatcher) {
        val updates = mutableMapOf<String, Any>(
            CHAT_UPDATED_AT_FIELD to readAtMillis
        )

        when (activeRole) {
            UserRole.PASSENGER -> updates[CHAT_PASSENGER_READ_AT_FIELD] = readAtMillis
            UserRole.DRIVER -> updates[CHAT_DRIVER_READ_AT_FIELD] = readAtMillis
        }

        firestore.collection(RIDES_COLLECTION)
            .document(rideId)
            .update(updates)
            .awaitResult()
    }

    private suspend fun parseRideThread(
        snapshot: com.google.firebase.firestore.DocumentSnapshot
    ): RideChatRemoteThread? {
        if (!snapshot.exists()) return null

        val rideId = snapshot.id
        val driverId = snapshot.getString(DRIVER_ID_FIELD).orEmpty()
        val passengerId = snapshot.readPassengerId()
        val driverName = snapshot.readDriverName(driverId)
        val passengerName = snapshot.readPassengerName(passengerId)
        val messages = snapshot.readMessages(rideId)
        val lastMessage = snapshot.getString(CHAT_LAST_MESSAGE_FIELD)
            .orEmpty()
            .ifBlank { messages.lastOrNull()?.content.orEmpty() }
        val lastMessageAtMillis = snapshot.getLong(CHAT_LAST_MESSAGE_AT_FIELD)
            ?: messages.lastOrNull()?.sentAtMillis
            ?: 0L
        val lastSenderId = snapshot.getString(CHAT_LAST_SENDER_ID_FIELD)
            .orEmpty()
            .ifBlank { messages.lastOrNull()?.senderId.orEmpty() }

        return RideChatRemoteThread(
            rideId = rideId,
            driverId = driverId,
            driverName = driverName,
            passengerId = passengerId,
            passengerName = passengerName,
            messages = messages,
            passengerReadAtMillis = snapshot.getLong(CHAT_PASSENGER_READ_AT_FIELD) ?: 0L,
            driverReadAtMillis = snapshot.getLong(CHAT_DRIVER_READ_AT_FIELD) ?: 0L,
            lastMessage = lastMessage,
            lastMessageAtMillis = lastMessageAtMillis,
            lastSenderId = lastSenderId
        )
    }

    private suspend fun com.google.firebase.firestore.DocumentSnapshot.readDriverName(
        driverId: String
    ): String {
        val storedName = getString(CHAT_DRIVER_NAME_FIELD).orEmpty()
        if (storedName.isNotBlank()) return storedName

        return resolveUserDisplayName(driverId, fallback = "Driver")
    }

    private suspend fun com.google.firebase.firestore.DocumentSnapshot.readPassengerName(
        passengerId: String
    ): String {
        val storedName = getString(CHAT_PASSENGER_NAME_FIELD).orEmpty()
        if (storedName.isNotBlank()) return storedName

        return if (passengerId.isBlank()) {
            "Passenger"
        } else {
            resolveUserDisplayName(passengerId, fallback = "Passenger")
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.readPassengerId(): String {
        val existingId = getString(CHAT_PASSENGER_ID_FIELD).orEmpty()
        if (existingId.isNotBlank()) return existingId

        val passengerIds = get(PASSENGER_IDS_FIELD)
            ?.let { raw -> raw as? List<*> }
            .orEmpty()
            .mapNotNull { it as? String }
            .filter { it.isNotBlank() }

        return passengerIds.firstOrNull().orEmpty()
    }

    private suspend fun com.google.firebase.firestore.DocumentSnapshot.readMessages(
        rideId: String
    ): List<RideChatRemoteMessage> {
        val rawMessages = get(CHAT_MESSAGES_FIELD)
            ?.let { raw -> raw as? List<*> }
            .orEmpty()

        val messages = mutableListOf<RideChatRemoteMessage>()
        for (entry in rawMessages) {
            val map = entry as? Map<*, *> ?: continue
            val messageId = map[REMOTE_MESSAGE_ID_FIELD] as? String ?: continue
            val senderId = map[REMOTE_MESSAGE_SENDER_ID_FIELD] as? String ?: continue
            val content = map[REMOTE_MESSAGE_CONTENT_FIELD] as? String ?: continue
            val sentAtMillis = (map[REMOTE_MESSAGE_SENT_AT_FIELD] as? Number)?.toLong() ?: 0L
            val fallbackName = if (senderId == getString(DRIVER_ID_FIELD)) "Driver" else "Passenger"
            val senderName = (map[REMOTE_MESSAGE_SENDER_NAME_FIELD] as? String).orEmpty()
                .ifBlank { resolveUserDisplayName(senderId, fallback = fallbackName) }

            messages += RideChatRemoteMessage(
                id = messageId,
                rideId = rideId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                sentAtMillis = sentAtMillis
            )
        }

        return messages.sortedBy { it.sentAtMillis }
    }

    private suspend fun resolveUserDisplayName(
        userId: String,
        fallback: String
    ): String {
        if (userId.isBlank()) return fallback

        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .awaitResult()

        return snapshot.getString("fullName")
            .orEmpty()
            .ifBlank {
                snapshot.getString("displayName")
                    .orEmpty()
                    .ifBlank {
                        snapshot.getString("email")
                            ?.substringBefore("@")
                            .orEmpty()
                    }
            }
            .ifBlank { fallback }
    }

    private fun RideChatRemoteMessage.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            REMOTE_MESSAGE_ID_FIELD to id,
            REMOTE_MESSAGE_RIDE_ID_FIELD to rideId,
            REMOTE_MESSAGE_SENDER_ID_FIELD to senderId,
            REMOTE_MESSAGE_SENDER_NAME_FIELD to senderName,
            REMOTE_MESSAGE_CONTENT_FIELD to content,
            REMOTE_MESSAGE_SENT_AT_FIELD to sentAtMillis
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    data class RideChatRemoteThread(
        val rideId: String,
        val driverId: String,
        val driverName: String,
        val passengerId: String,
        val passengerName: String,
        val messages: List<RideChatRemoteMessage>,
        val passengerReadAtMillis: Long,
        val driverReadAtMillis: Long,
        val lastMessage: String,
        val lastMessageAtMillis: Long,
        val lastSenderId: String
    )

    data class RideChatRemoteMessage(
        val id: String,
        val rideId: String,
        val senderId: String,
        val senderName: String,
        val content: String,
        val sentAtMillis: Long
    )

    private companion object {
        const val RIDES_COLLECTION = "rides"
        const val USERS_COLLECTION = "users"

        const val DRIVER_ID_FIELD = "driverId"
        const val PASSENGER_IDS_FIELD = "passengerIds"

        const val CHAT_MESSAGES_FIELD = "chatMessages"
        const val CHAT_HAS_MESSAGES_FIELD = "chatHasMessages"
        const val CHAT_LAST_MESSAGE_FIELD = "chatLastMessage"
        const val CHAT_LAST_MESSAGE_AT_FIELD = "chatLastMessageAtMillis"
        const val CHAT_LAST_SENDER_ID_FIELD = "chatLastSenderId"
        const val CHAT_PASSENGER_READ_AT_FIELD = "chatPassengerReadAtMillis"
        const val CHAT_DRIVER_READ_AT_FIELD = "chatDriverReadAtMillis"
        const val CHAT_UPDATED_AT_FIELD = "chatUpdatedAtMillis"
        const val CHAT_DRIVER_NAME_FIELD = "chatDriverName"
        const val CHAT_PASSENGER_NAME_FIELD = "chatPassengerName"
        const val CHAT_PASSENGER_ID_FIELD = "chatPassengerId"

        const val REMOTE_MESSAGE_ID_FIELD = "id"
        const val REMOTE_MESSAGE_RIDE_ID_FIELD = "rideId"
        const val REMOTE_MESSAGE_SENDER_ID_FIELD = "senderId"
        const val REMOTE_MESSAGE_SENDER_NAME_FIELD = "senderName"
        const val REMOTE_MESSAGE_CONTENT_FIELD = "content"
        const val REMOTE_MESSAGE_SENT_AT_FIELD = "sentAtMillis"
    }
}
