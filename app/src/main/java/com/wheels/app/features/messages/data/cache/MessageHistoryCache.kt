package com.wheels.app.features.messages.data.cache

import android.util.LruCache
import com.wheels.app.features.messages.data.remote.MessageRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hot in-memory cache for recent chat histories.
 *
 * The app keeps inbox metadata in Room, but the actual message bubbles stay here so we do not
 * bloat the SQLite database with full conversation history. The cache uses LRU eviction, which
 * means the least recently accessed conversation is removed first when the limit is exceeded.
 */
@Singleton
class MessageHistoryCache @Inject constructor() {

    private val cache = object : LruCache<String, MessageRemoteDataSource.RideChatRemoteThread>(
        MAX_CONVERSATIONS
    ) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String?,
            oldValue: MessageRemoteDataSource.RideChatRemoteThread?,
            newValue: MessageRemoteDataSource.RideChatRemoteThread?
        ) {
            if (evicted && key != null) {
                // Eviction is intentional: once a thread falls out of the hot set, we let it go.
            }
        }
    }

    /**
     * Reads the cached history for one ride.
     *
     * If the thread is present, it becomes the most recently used entry and is kept alive longer.
     */
    fun get(rideId: String): MessageRemoteDataSource.RideChatRemoteThread? {
        return synchronized(cache) { cache.get(rideId) }
    }

    /**
     * Stores a conversation history in the hot set.
     *
     * We trim each conversation to the latest messages so the cache stays small and predictable.
     */
    fun put(thread: MessageRemoteDataSource.RideChatRemoteThread) {
        val trimmedMessages = thread.messages.takeLast(MAX_MESSAGES_PER_CONVERSATION)
        val trimmedThread = if (trimmedMessages.size == thread.messages.size) {
            thread
        } else {
            thread.copy(messages = trimmedMessages)
        }

        synchronized(cache) {
            cache.put(trimmedThread.rideId, trimmedThread)
        }
    }

    fun remove(rideId: String) {
        synchronized(cache) { cache.remove(rideId) }
    }

    fun clear() {
        synchronized(cache) { cache.evictAll() }
    }

    companion object {
        private const val MAX_CONVERSATIONS = 25
        private const val MAX_MESSAGES_PER_CONVERSATION = 25
    }
}
