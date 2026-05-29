package com.wheels.app.features.messages.presentation.mock

import com.wheels.app.features.messages.presentation.model.MessageBubbleUiModel
import com.wheels.app.features.messages.presentation.model.MessageThreadUiModel

object MessagesMockData {

    private val threads = listOf(
        MessageThreadUiModel(
            id = "camila-rojas",
            participantName = "Camila Rojas",
            participantInitials = "CR",
            participantStatus = "Online now",
            rideContext = "Campus Uniandes -> Centro Andino",
            isUnread = true,
            messages = listOf(
                MessageBubbleUiModel(
                    id = "cr-1",
                    content = "I'm at the north entrance with the blue backpack.",
                    timestamp = "8:31 AM",
                    isCurrentUser = false
                ),
                MessageBubbleUiModel(
                    id = "cr-2",
                    content = "Perfect, I'm pulling up now.",
                    timestamp = "8:32 AM",
                    isCurrentUser = true
                ),
                MessageBubbleUiModel(
                    id = "cr-3",
                    content = "I can see you. You're good to stop by the curb.",
                    timestamp = "8:42 AM",
                    isCurrentUser = false
                )
            )
        ),
        MessageThreadUiModel(
            id = "andres-vega",
            participantName = "Andres Vega",
            participantInitials = "AV",
            participantStatus = "Last seen 4m ago",
            rideContext = "Usaquén -> Zona T",
            isUnread = false,
            messages = listOf(
                MessageBubbleUiModel(
                    id = "av-1",
                    content = "The driver is 5 minutes away.",
                    timestamp = "7:18 AM",
                    isCurrentUser = false
                ),
                MessageBubbleUiModel(
                    id = "av-2",
                    content = "Great, I’ll be ready by the lobby.",
                    timestamp = "7:19 AM",
                    isCurrentUser = true
                ),
                MessageBubbleUiModel(
                    id = "av-3",
                    content = "Perfect, I’m heading down now.",
                    timestamp = "7:23 AM",
                    isCurrentUser = false
                )
            )
        ),
        MessageThreadUiModel(
            id = "sofia-perez",
            participantName = "Sofia Perez",
            participantInitials = "SP",
            participantStatus = "Online now",
            rideContext = "Chapinero -> Salitre Plaza",
            isUnread = true,
            messages = listOf(
                MessageBubbleUiModel(
                    id = "sp-1",
                    content = "Thanks for sharing the pickup pin.",
                    timestamp = "6:10 PM",
                    isCurrentUser = false
                ),
                MessageBubbleUiModel(
                    id = "sp-2",
                    content = "No problem, I’ll wait by the main entrance.",
                    timestamp = "6:11 PM",
                    isCurrentUser = true
                ),
                MessageBubbleUiModel(
                    id = "sp-3",
                    content = "I can see the car now. See you in a sec.",
                    timestamp = "6:13 PM",
                    isCurrentUser = false
                )
            )
        ),
        MessageThreadUiModel(
            id = "mateo-ruiz",
            participantName = "Mateo Ruiz",
            participantInitials = "MR",
            participantStatus = "Last seen 1h ago",
            rideContext = "El Nogal -> Carrera 15",
            isUnread = false,
            messages = listOf(
                MessageBubbleUiModel(
                    id = "mr-1",
                    content = "I'll be there in 3 minutes.",
                    timestamp = "11:02 AM",
                    isCurrentUser = false
                ),
                MessageBubbleUiModel(
                    id = "mr-2",
                    content = "Perfect, I'll hold the spot.",
                    timestamp = "11:03 AM",
                    isCurrentUser = true
                ),
                MessageBubbleUiModel(
                    id = "mr-3",
                    content = "Thanks!",
                    timestamp = "11:04 AM",
                    isCurrentUser = false
                )
            )
        )
    )

    fun inboxThreads(): List<MessageThreadUiModel> = threads

    fun conversation(conversationId: String): MessageThreadUiModel {
        return threads.firstOrNull { it.id == conversationId } ?: threads.first()
    }

    fun defaultConversationId(): String = threads.first().id
}
