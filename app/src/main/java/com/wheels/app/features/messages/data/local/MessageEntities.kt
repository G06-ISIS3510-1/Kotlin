package com.wheels.app.features.messages.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wheels.app.features.messages.domain.model.MessageConversation
import com.wheels.app.features.messages.domain.model.buildInitials

/**
 * Durable inbox metadata stored in SQLite.
 *
 * We intentionally keep this table lightweight: it only stores the preview data that the
 * inbox card needs to render instantly after app launch or while offline.
 *
 * Full message history lives in an in-memory LRU cache and is rehydrated from Firestore
 * whenever the network is available.
 */
@Entity(
    tableName = "message_conversations",
    primaryKeys = ["ownerUserId", "rideId"],
    indices = [
        Index(value = ["ownerUserId"]),
        Index(value = ["ownerUserId", "lastMessageAtMillis"])
    ]
)
data class MessageConversationEntity(
    /**
     * The authenticated user that owns this preview row.
     *
     * We keep this in the primary key so passenger and driver views never overwrite each
     * other when the same ride is opened under different accounts on the same device.
     */
    val ownerUserId: String = "",
    val rideId: String = "",
    val participantName: String = "",
    val lastMessage: String = "",
    val lastMessageAtMillis: Long = 0L,
    val isNewMessage: Boolean = false
)

fun MessageConversationEntity.toDomain(): MessageConversation {
    val resolvedName = participantName.ifBlank { "Conversation" }

    return MessageConversation(
        rideId = rideId,
        participantName = resolvedName,
        participantInitials = buildInitials(resolvedName),
        lastMessage = lastMessage,
        lastMessageAtMillis = lastMessageAtMillis,
        isUnread = isNewMessage
    )
}
