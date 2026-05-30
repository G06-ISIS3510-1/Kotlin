package com.wheels.app.features.messages.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query(
        """
        SELECT * FROM message_conversations
        WHERE ownerUserId = :userId
        ORDER BY lastMessageAtMillis DESC
        """
    )
    fun observeInbox(userId: String): Flow<List<MessageConversationEntity>>

    @Query(
        """
        SELECT * FROM message_conversations
        WHERE ownerUserId = :userId AND rideId = :rideId
        LIMIT 1
        """
    )
    fun observeConversation(userId: String, rideId: String): Flow<MessageConversationEntity?>

    @Query(
        """
        SELECT * FROM message_conversations
        WHERE ownerUserId = :userId AND rideId = :rideId
        LIMIT 1
        """
    )
    suspend fun getConversation(userId: String, rideId: String): MessageConversationEntity?

    @Upsert
    suspend fun upsertConversation(conversation: MessageConversationEntity)

    @Query(
        """
        UPDATE message_conversations
        SET isNewMessage = 0
        WHERE ownerUserId = :userId AND rideId = :rideId
        """
    )
    suspend fun markConversationRead(userId: String, rideId: String)
}
