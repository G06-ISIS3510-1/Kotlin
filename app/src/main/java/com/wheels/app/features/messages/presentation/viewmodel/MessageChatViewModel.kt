package com.wheels.app.features.messages.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.features.messages.domain.model.MessageBubble
import com.wheels.app.features.messages.domain.model.MessageThread
import com.wheels.app.features.messages.domain.repository.MessageRepository
import com.wheels.app.features.messages.presentation.model.MessageBubbleUiModel
import com.wheels.app.features.messages.presentation.model.MessageThreadUiModel
import com.wheels.app.features.profile.domain.model.User
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
class MessageChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val messageRepository: MessageRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val rideId = savedStateHandle.get<String>(Destinations.MESSAGE_CHAT_RIDE_ID_KEY).orEmpty()
    private var currentUser: User? = null

    private val _uiState = MutableStateFlow(
        MessageChatUiState(
            isLoading = true,
            isOnline = networkMonitor.isOnline()
        )
    )
    val uiState: StateFlow<MessageChatUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeConversation()
    }

    /**
     * Sends one message after validating the current network state.
     *
     * The UI keeps the draft text until this method returns `true`. That way we do not clear
     * the composer when the network is offline or Firestore rejects the write.
     */
    suspend fun sendMessage(content: String): Boolean {
        val trimmed = content.trim()
        val user = currentUser ?: return false

        if (trimmed.isBlank() || rideId.isBlank() || !networkMonitor.isOnline()) {
            return false
        }

        return runCatching {
            messageRepository.sendMessage(
                rideId = rideId,
                senderId = user.id,
                senderName = user.fullName.ifBlank {
                    user.email.substringBefore("@").ifBlank { user.activeRole.displayName }
                },
                activeRole = user.activeRole,
                content = trimmed
            )
        }.isSuccess
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isOnline()
            networkMonitor.observeIsOnline().collectLatest { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)

                // When connectivity comes back we re-seed the thread so the local DB and the
                // in-memory LRU pick up any messages that arrived while the app was disconnected.
                if (isOnline && !wasOnline) {
                    refreshConversationIfPossible()
                }

                wasOnline = isOnline
            }
        }
    }

    private fun observeConversation() {
        viewModelScope.launch {
            getUserProfileUseCase().collectLatest { user ->
                currentUser = user

                if (user == null || rideId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        thread = null,
                        isLoading = false
                    )
                    return@collectLatest
                }

                refreshConversationIfPossible(user)

                messageRepository
                    .observeConversation(
                        rideId = rideId,
                        userId = user.id,
                        activeRole = user.activeRole
                    )
                    .catch {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .collect { thread ->
                        val keepLoading = thread == null && _uiState.value.isOnline
                        _uiState.value = _uiState.value.copy(
                            thread = thread?.toUiModel(),
                            isLoading = keepLoading
                        )
                    }
            }
        }
    }

    /**
     * Pulls the latest thread from Firestore and marks it as read.
     *
     * This helper is intentionally small and side-effect focused: the repository decides how to
     * merge the network snapshot into Room and the LRU cache.
     */
    private fun refreshConversationIfPossible(user: User? = currentUser) {
        val resolvedUser = user ?: return
        if (rideId.isBlank() || !networkMonitor.isOnline()) return

        viewModelScope.launch {
            runCatching {
                messageRepository.seedConversation(
                    rideId = rideId,
                    userId = resolvedUser.id,
                    activeRole = resolvedUser.activeRole
                )
            }

            runCatching {
                messageRepository.markConversationRead(
                    rideId = rideId,
                    userId = resolvedUser.id,
                    activeRole = resolvedUser.activeRole
                )
            }
        }
    }
}

data class MessageChatUiState(
    val thread: MessageThreadUiModel? = null,
    val isLoading: Boolean = false,
    val isOnline: Boolean = false
)

private fun MessageThread.toUiModel(): MessageThreadUiModel {
    return MessageThreadUiModel(
        id = rideId,
        participantName = participantName,
        participantInitials = participantInitials,
        participantStatus = participantStatus,
        rideContext = "",
        isUnread = false,
        messages = messages.map { it.toUiModel() },
        canSendMessages = canSendMessages
    )
}

private fun MessageBubble.toUiModel(): MessageBubbleUiModel {
    return MessageBubbleUiModel(
        id = id,
        content = content,
        timestamp = formatTime(sentAtMillis),
        isCurrentUser = isCurrentUser
    )
}

private fun formatTime(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return ""
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestampMillis))
}
