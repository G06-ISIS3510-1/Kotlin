package com.wheels.app.features.messages.data.repository

import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.messages.data.cache.MessageHistoryCache
import com.wheels.app.features.messages.data.local.MessageConversationEntity
import com.wheels.app.features.messages.data.local.MessageLocalDataSource
import com.wheels.app.features.messages.data.local.toDomain
import com.wheels.app.features.messages.data.remote.MessageRemoteDataSource
import com.wheels.app.features.messages.data.remote.MessageRemoteDataSource.RideChatRemoteMessage
import com.wheels.app.features.messages.data.remote.MessageRemoteDataSource.RideChatRemoteThread
import com.wheels.app.features.messages.domain.model.MessageBubble
import com.wheels.app.features.messages.domain.model.MessageConversation
import com.wheels.app.features.messages.domain.model.MessageThread
import com.wheels.app.features.messages.domain.model.buildInitials
import com.wheels.app.features.messages.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class FirestoreRideMessageRepository @Inject constructor(
    private val remoteDataSource: MessageRemoteDataSource,
    private val localDataSource: MessageLocalDataSource,
    private val messageHistoryCache: MessageHistoryCache,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher
) : MessageRepository {

    override fun observeInbox(
        userId: String,
        activeRole: UserRole
    ): Flow<List<MessageConversation>> {
        if (userId.isBlank()) {
            return flowOf(emptyList())
        }

        return callbackFlow {
            // Step 1: emit the Room-backed inbox immediately so the list renders instantly.
            val localJob = launch {
                val localInbox = when (activeRole) {
                    UserRole.PASSENGER -> localDataSource.observePassengerInbox(userId)
                    UserRole.DRIVER -> localDataSource.observeDriverInbox(userId)
                }

                localInbox.collect { conversations ->
                    trySend(conversations.map { it.toDomain() })
                }
            }

            // Step 2: listen to Firestore. Every remote snapshot refreshes the LRU cache and
            // updates the small Room metadata row so the inbox stays warm across app restarts.
            val remoteJob = launch {
                remoteDataSource.observeInbox(userId, activeRole).collect { threads ->
                    withContext(ioDispatcher) {
                        threads.forEach { thread ->
                            try {
                                syncRemoteThread(
                                    thread = thread,
                                    ownerUserId = userId,
                                    activeRole = activeRole
                                )
                            } catch (_: Throwable) {
                                // One malformed thread should not block the rest of the inbox.
                            }
                        }
                    }
                }
            }

            awaitClose {
                localJob.cancel()
                remoteJob.cancel()
            }
        }
    }

    override fun observeConversation(
        rideId: String,
        userId: String,
        activeRole: UserRole
    ): Flow<MessageThread?> {
        if (rideId.isBlank() || userId.isBlank()) {
            return flowOf(null)
        }

        return callbackFlow {
            var latestConversation: MessageConversationEntity? = null
            var latestCachedThread: RideChatRemoteThread? = messageHistoryCache.get(rideId)

            // The UI should be able to render from either source:
            // - cached chat history when we already have it in memory
            // - Room metadata when the cache was evicted
            // - Firestore when a fresh snapshot arrives
            fun emitCurrentThread() {
                val thread = latestCachedThread?.toDomain(userId, activeRole)
                    ?: latestConversation?.toThread(activeRole)

                trySend(thread)
            }

            val localJob = launch {
                localDataSource.observeConversation(userId, rideId).collect { conversation ->
                    latestConversation = conversation
                    emitCurrentThread()
                }
            }

            val remoteJob = launch {
                remoteDataSource.observeRideChat(rideId).collect { thread ->
                    if (thread == null) return@collect

                    withContext(ioDispatcher) {
                        try {
                            syncRemoteThread(
                                thread = thread,
                                ownerUserId = userId,
                                activeRole = activeRole
                            )
                        } catch (_: Throwable) {
                            // We keep the in-memory thread visible even if Room sync fails.
                        }
                    }

                    val cachedThread = messageHistoryCache.get(rideId) ?: thread
                    latestCachedThread = cachedThread
                    trySend(cachedThread.toDomain(userId, activeRole))
                }
            }

            awaitClose {
                localJob.cancel()
                remoteJob.cancel()
            }
        }
    }

    override suspend fun sendMessage(
        rideId: String,
        senderId: String,
        senderName: String,
        activeRole: UserRole,
        content: String
    ) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) return
        if (!networkMonitor.isOnline()) {
            throw IllegalStateException("An internet connection is required to send messages.")
        }

        withContext(ioDispatcher) {
            // We fetch the latest remote thread first so the send path uses authoritative
            // ride data instead of relying on possibly stale local cache state.
            val currentThread = loadThreadForMutation(rideId)
                ?: throw IllegalStateException("Conversation is not ready yet.")

            // Drivers can reply once a passenger has already opened the conversation.
            if (activeRole == UserRole.DRIVER && currentThread.messages.isEmpty()) {
                throw IllegalStateException("Drivers can reply after a passenger starts the conversation.")
            }

            val now = System.currentTimeMillis()
            val messageId = UUID.randomUUID().toString()
            val remoteMessage = RideChatRemoteMessage(
                id = messageId,
                rideId = rideId,
                senderId = senderId,
                senderName = senderName,
                content = trimmedContent,
                sentAtMillis = now
            )

            remoteDataSource.sendMessage(
                rideId = rideId,
                message = remoteMessage,
                activeRole = activeRole
            )

            // After a successful write we fetch the fresh Firestore thread and push it into the
            // hot cache. This keeps the in-memory chat history and the Room preview aligned.
            val refreshedThread = remoteDataSource.fetchRideChat(rideId)
                ?: currentThread.copy(
                    messages = currentThread.messages + remoteMessage,
                    lastMessage = trimmedContent,
                    lastMessageAtMillis = now,
                    lastSenderId = senderId,
                    passengerReadAtMillis = if (activeRole == UserRole.PASSENGER) now else currentThread.passengerReadAtMillis,
                    driverReadAtMillis = if (activeRole == UserRole.DRIVER) now else currentThread.driverReadAtMillis
                )

            messageHistoryCache.put(refreshedThread)
            syncRemoteThread(
                thread = refreshedThread,
                ownerUserId = senderId,
                activeRole = activeRole
            )
        }
    }

    override suspend fun markConversationRead(
        rideId: String,
        userId: String,
        activeRole: UserRole
    ) {
        if (rideId.isBlank() || userId.isBlank()) return

        withContext(ioDispatcher) {
            val now = System.currentTimeMillis()

            // Update the local preview immediately so the unread dot disappears even if the
            // remote read receipt is delayed or the device just came back online.
            localDataSource.markConversationRead(userId, rideId)

            val cachedThread = messageHistoryCache.get(rideId)
            if (cachedThread != null) {
                val updatedCacheThread = cachedThread.copy(
                    passengerReadAtMillis = if (activeRole == UserRole.PASSENGER) now else cachedThread.passengerReadAtMillis,
                    driverReadAtMillis = if (activeRole == UserRole.DRIVER) now else cachedThread.driverReadAtMillis
                )
                messageHistoryCache.put(updatedCacheThread)
                syncConversationPreview(
                    thread = updatedCacheThread,
                    ownerUserId = userId,
                    activeRole = activeRole
                )
            }

            if (!networkMonitor.isOnline()) return@withContext

            try {
                remoteDataSource.markConversationRead(
                    rideId = rideId,
                    activeRole = activeRole,
                    readAtMillis = now
                )
            } catch (_: Throwable) {
                // The local unread flag is already updated; the remote read receipt can catch up.
            }
        }
    }

    override suspend fun seedConversation(
        rideId: String,
        userId: String,
        activeRole: UserRole
    ) {
        if (rideId.isBlank() || userId.isBlank()) return
        if (!networkMonitor.isOnline()) return

        withContext(ioDispatcher) {
            val thread = remoteDataSource.fetchRideChat(rideId) ?: return@withContext
            syncRemoteThread(
                thread = thread,
                ownerUserId = userId,
                activeRole = activeRole
            )
        }
    }

    /**
     * Keeps the local preview row and the hot cache in sync with a Firestore thread snapshot.
     *
     * Room only stores inbox metadata, so we only persist a row when the thread actually has
     * message content. Empty threads stay in memory and can still render in the chat view.
     */
    private suspend fun syncRemoteThread(
        thread: RideChatRemoteThread,
        ownerUserId: String,
        activeRole: UserRole
    ) {
        messageHistoryCache.put(thread)
        syncConversationPreview(
            thread = thread,
            ownerUserId = ownerUserId,
            activeRole = activeRole
        )
    }

    /**
     * Writes the inbox-card projection to Room.
     *
     * The database row is deliberately small: it only contains the fields needed to render the
     * inbox card quickly after launch or while offline.
     */
    private suspend fun syncConversationPreview(
        thread: RideChatRemoteThread,
        ownerUserId: String,
        activeRole: UserRole
    ) {
        if (thread.messages.isEmpty() && thread.lastMessage.isBlank()) {
            return
        }

        localDataSource.upsertConversation(
            thread.toConversationEntity(
                ownerUserId = ownerUserId,
                activeRole = activeRole
            )
        )
    }

    /**
     * Loads the latest thread for a mutation.
     *
     * Firestore is the authoritative source for the thread shape, but we still keep the LRU
     * cache as a fast fallback if the network fetch fails after the app has already warmed the
     * conversation once.
     */
    private suspend fun loadThreadForMutation(rideId: String): RideChatRemoteThread? {
        messageHistoryCache.get(rideId)?.let { cachedThread ->
            if (cachedThread.messages.isNotEmpty()) {
                return cachedThread
            }
        }

        return runCatching {
            remoteDataSource.fetchRideChat(rideId)
        }.getOrNull()?.also { refreshedThread ->
            messageHistoryCache.put(refreshedThread)
        } ?: messageHistoryCache.get(rideId)
    }
}

/**
 * Converts a remote Firestore thread into the small Room preview row.
 *
 * The preview needs the other participant's name, the latest message summary, the timestamp,
 * and the unread flag. Everything else stays out of SQLite and lives either in memory or on
 * Firestore.
 */
private fun RideChatRemoteThread.toConversationEntity(
    ownerUserId: String,
    activeRole: UserRole
): MessageConversationEntity {
    val resolvedParticipantName = participantName(activeRole)
    val lastReadAtMillis = when (activeRole) {
        UserRole.PASSENGER -> passengerReadAtMillis
        UserRole.DRIVER -> driverReadAtMillis
    }

    return MessageConversationEntity(
        ownerUserId = ownerUserId,
        rideId = rideId,
        participantName = resolvedParticipantName,
        lastMessage = lastMessage,
        lastMessageAtMillis = lastMessageAtMillis,
        isNewMessage = lastMessage.isNotBlank() &&
            lastMessageAtMillis > lastReadAtMillis &&
            lastSenderId.isNotBlank() &&
            lastSenderId != ownerUserId
    )
}

/**
 * Builds the in-memory chat thread directly from the cached Firestore snapshot.
 *
 * This path is used whenever the hot cache already holds the recent message history. It keeps
 * the message list fast while letting Room continue to act as the durable inbox summary.
 */
private fun RideChatRemoteThread.toDomain(
    currentUserId: String,
    activeRole: UserRole
): MessageThread {
    val resolvedParticipantName = participantName(activeRole)

    return MessageThread(
        rideId = rideId,
        participantName = resolvedParticipantName,
        participantInitials = buildInitials(resolvedParticipantName),
        participantStatus = participantStatus(activeRole),
        canSendMessages = when (activeRole) {
            UserRole.PASSENGER -> true
            UserRole.DRIVER -> messages.isNotEmpty()
        },
        messages = messages.map { it.toDomain(currentUserId) }
    )
}

/**
 * Builds a lightweight thread model from Room-only metadata when the LRU cache is cold.
 *
 * This still gives the chat screen enough information to render the header and composer while
 * we wait for Firestore to repopulate the cache.
 */
private fun MessageConversationEntity.toThread(activeRole: UserRole): MessageThread {
    val resolvedParticipantName = participantName.ifBlank {
        when (activeRole) {
            UserRole.PASSENGER -> "Driver"
            UserRole.DRIVER -> "Passenger"
        }
    }

    return MessageThread(
        rideId = rideId,
        participantName = resolvedParticipantName,
        participantInitials = buildInitials(resolvedParticipantName),
        participantStatus = participantStatus(activeRole),
        canSendMessages = when (activeRole) {
            UserRole.PASSENGER -> true
            UserRole.DRIVER -> lastMessage.isNotBlank()
        },
        messages = emptyList()
    )
}

/**
 * Maps a single Firestore message into the UI domain model.
 *
 * The bubble alignment is determined by whether the sender matches the signed-in user.
 */
private fun RideChatRemoteMessage.toDomain(currentUserId: String): MessageBubble {
    return MessageBubble(
        id = id,
        content = content,
        sentAtMillis = sentAtMillis,
        isCurrentUser = senderId == currentUserId
    )
}

private fun RideChatRemoteThread.participantName(activeRole: UserRole): String {
    return when (activeRole) {
        UserRole.PASSENGER -> driverName.ifBlank { "Driver" }
        UserRole.DRIVER -> passengerName.ifBlank { "Passenger" }
    }
}

private fun participantStatus(activeRole: UserRole): String {
    return when (activeRole) {
        UserRole.PASSENGER -> "Driver"
        UserRole.DRIVER -> "Passenger"
    }
}
