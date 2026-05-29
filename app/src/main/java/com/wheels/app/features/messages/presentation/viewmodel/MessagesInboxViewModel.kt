package com.wheels.app.features.messages.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheels.app.features.messages.presentation.mock.MessagesMockData
import com.wheels.app.features.messages.presentation.model.MessageConversationPreviewUiModel
import com.wheels.app.features.messages.presentation.model.toPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MessagesInboxViewModel @Inject constructor() : ViewModel() {

    private val threads = MessagesMockData.inboxThreads()

    private val _uiState = MutableStateFlow(
        MessagesInboxUiState(
            conversations = threads.map { it.toPreview() }
        )
    )

    val uiState: StateFlow<MessagesInboxUiState> = _uiState.asStateFlow()
}

data class MessagesInboxUiState(
    val conversations: List<MessageConversationPreviewUiModel> = emptyList()
)
