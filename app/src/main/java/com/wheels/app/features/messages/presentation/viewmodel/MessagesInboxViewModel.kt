package com.wheels.app.features.messages.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.messages.domain.model.MessageConversation
import com.wheels.app.features.messages.domain.repository.MessageRepository
import com.wheels.app.features.messages.presentation.model.MessageConversationPreviewUiModel
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class MessagesInboxViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesInboxUiState(isLoading = true))
    val uiState: StateFlow<MessagesInboxUiState> = _uiState.asStateFlow()

    init {
        observeInbox()
    }

    private fun observeInbox() {
        viewModelScope.launch {
            getUserProfileUseCase().collectLatest { user ->
                if (user == null) {
                    _uiState.value = MessagesInboxUiState(isLoading = false)
                    return@collectLatest
                }

                messageRepository
                    .observeInbox(user.id, user.activeRole)
                    .catch {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .collect { conversations ->
                        _uiState.value = MessagesInboxUiState(
                            conversations = conversations.map { it.toUiModel() },
                            isLoading = false
                        )
                    }
            }
        }
    }
}

data class MessagesInboxUiState(
    val conversations: List<MessageConversationPreviewUiModel> = emptyList(),
    val isLoading: Boolean = false
)

private fun MessageConversation.toUiModel(): MessageConversationPreviewUiModel {
    return MessageConversationPreviewUiModel(
        id = rideId,
        participantName = participantName,
        participantInitials = participantInitials,
        lastMessage = lastMessage,
        lastMessageTime = formatTime(lastMessageAtMillis),
        isUnread = isUnread
    )
}

private fun formatTime(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return ""
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestampMillis))
}
