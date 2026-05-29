package com.wheels.app.features.messages.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.features.messages.presentation.mock.MessagesMockData
import com.wheels.app.features.messages.presentation.model.MessageThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MessageChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId = savedStateHandle.get<String>(Destinations.MESSAGE_CHAT_CONVERSATION_ID_KEY)
        ?: MessagesMockData.defaultConversationId()

    private val _uiState = MutableStateFlow(
        MessageChatUiState(
            thread = MessagesMockData.conversation(conversationId)
        )
    )

    val uiState: StateFlow<MessageChatUiState> = _uiState.asStateFlow()
}

data class MessageChatUiState(
    val thread: MessageThreadUiModel? = null
)
