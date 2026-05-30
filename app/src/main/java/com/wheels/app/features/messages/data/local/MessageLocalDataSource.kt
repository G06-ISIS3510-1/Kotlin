package com.wheels.app.features.messages.data.local

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessageLocalDataSource @Inject constructor(
    private val dao: MessageDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun observePassengerInbox(userId: String): Flow<List<MessageConversationEntity>> {
        return dao.observeInbox(userId)
    }

    fun observeDriverInbox(userId: String): Flow<List<MessageConversationEntity>> {
        return dao.observeInbox(userId)
    }

    fun observeConversation(userId: String, rideId: String): Flow<MessageConversationEntity?> {
        return dao.observeConversation(userId, rideId)
    }

    suspend fun getConversation(userId: String, rideId: String): MessageConversationEntity? =
        withContext(ioDispatcher) {
            dao.getConversation(userId, rideId)
        }

    suspend fun upsertConversation(conversation: MessageConversationEntity) =
        withContext(ioDispatcher) {
            dao.upsertConversation(conversation)
        }

    suspend fun markConversationRead(
        userId: String,
        rideId: String
    ) = withContext(ioDispatcher) {
        dao.markConversationRead(userId, rideId)
    }
}
