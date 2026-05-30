package com.wheels.app.features.messages.domain.repository

import com.wheels.app.core.session.UserRole
import com.wheels.app.features.messages.domain.model.MessageConversation
import com.wheels.app.features.messages.domain.model.MessageThread
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeInbox(userId: String, activeRole: UserRole): Flow<List<MessageConversation>>

    fun observeConversation(
        rideId: String,
        userId: String,
        activeRole: UserRole
    ): Flow<MessageThread?>

    suspend fun sendMessage(
        rideId: String,
        senderId: String,
        senderName: String,
        activeRole: UserRole,
        content: String
    )

    suspend fun markConversationRead(
        rideId: String,
        userId: String,
        activeRole: UserRole
    )

    suspend fun seedConversation(
        rideId: String,
        userId: String,
        activeRole: UserRole
    )
}
