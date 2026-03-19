package com.wheels.app.features.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        GroupChatUiState(
            participantsLabel = "Carlos + Laura + Mateo + Juliana",
            messages = listOf(
                ChatMessageUiModel(
                    id = "1",
                    senderName = "Carlos",
                    content = "Hola equipo, ya voy saliendo de la porteria de Uniandes.",
                    timestamp = "8:12 PM",
                    isCurrentUser = false
                ),
                ChatMessageUiModel(
                    id = "2",
                    senderName = "Laura",
                    content = "Perfecto, yo ya estoy en la entrada principal.",
                    timestamp = "8:13 PM",
                    isCurrentUser = false
                ),
                ChatMessageUiModel(
                    id = "3",
                    senderName = "Tú",
                    content = "Voy en 2 minutos, gracias por esperar.",
                    timestamp = "8:14 PM",
                    isCurrentUser = true
                ),
                ChatMessageUiModel(
                    id = "4",
                    senderName = "Mateo",
                    content = "Yo también ya estoy bajando.",
                    timestamp = "8:15 PM",
                    isCurrentUser = false
                )
            )
        )
    )
    val uiState: StateFlow<GroupChatUiState> = _uiState.asStateFlow()

    fun onEvent(event: GroupChatEvent) {
        when (event) {
            is GroupChatEvent.DraftChanged -> {
                _uiState.update { it.copy(draftMessage = event.value) }
            }

            GroupChatEvent.SendMessage -> sendMessage()
        }
    }

    private fun sendMessage() {
        val newMessage = _uiState.value.draftMessage.trim()
        if (newMessage.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                draftMessage = "",
                messages = state.messages + ChatMessageUiModel(
                    id = (state.messages.size + 1).toString(),
                    senderName = "Tú",
                    content = newMessage,
                    timestamp = "Ahora",
                    isCurrentUser = true
                )
            )
        }
    }
}

sealed interface GroupChatEvent {
    data class DraftChanged(val value: String) : GroupChatEvent
    data object SendMessage : GroupChatEvent
}

data class GroupChatUiState(
    val title: String = "Ride Group Chat",
    val tripLabel: String = "Campus Uniandes -> Centro Andino",
    val participantsLabel: String,
    val draftMessage: String = "",
    val messages: List<ChatMessageUiModel> = emptyList()
)

data class ChatMessageUiModel(
    val id: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isCurrentUser: Boolean
)
