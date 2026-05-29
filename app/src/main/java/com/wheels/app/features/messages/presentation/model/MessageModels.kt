package com.wheels.app.features.messages.presentation.model

data class MessageConversationPreviewUiModel(
    val id: String,
    val participantName: String,
    val participantInitials: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val isUnread: Boolean
)

data class MessageBubbleUiModel(
    val id: String,
    val content: String,
    val timestamp: String,
    val isCurrentUser: Boolean
)

data class MessageThreadUiModel(
    val id: String,
    val participantName: String,
    val participantInitials: String,
    val participantStatus: String,
    val rideContext: String,
    val isUnread: Boolean,
    val messages: List<MessageBubbleUiModel>
)

fun MessageThreadUiModel.toPreview(): MessageConversationPreviewUiModel {
    val latestMessage = messages.lastOrNull()

    return MessageConversationPreviewUiModel(
        id = id,
        participantName = participantName,
        participantInitials = participantInitials,
        lastMessage = latestMessage?.content.orEmpty(),
        lastMessageTime = latestMessage?.timestamp.orEmpty(),
        isUnread = isUnread
    )
}
