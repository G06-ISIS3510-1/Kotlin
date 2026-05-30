package com.wheels.app.features.messages.domain.model

import com.wheels.app.core.session.UserRole

data class MessageConversation(
    val rideId: String,
    val participantName: String,
    val participantInitials: String,
    val lastMessage: String,
    val lastMessageAtMillis: Long,
    val isUnread: Boolean
)

data class MessageBubble(
    val id: String,
    val content: String,
    val sentAtMillis: Long,
    val isCurrentUser: Boolean
)

data class MessageThread(
    val rideId: String,
    val participantName: String,
    val participantInitials: String,
    val participantStatus: String,
    val canSendMessages: Boolean,
    val messages: List<MessageBubble>
)

fun buildInitials(displayName: String): String {
    return displayName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank {
            if (displayName.isNotBlank()) {
                displayName.take(2).uppercase()
            } else {
                "?"
            }
        }
}

fun MessageConversation.pickParticipantName(role: UserRole): String {
    return participantName.ifBlank {
        when (role) {
            UserRole.PASSENGER -> "Driver"
            UserRole.DRIVER -> "Passenger"
        }
    }
}
