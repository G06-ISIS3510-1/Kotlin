package com.wheels.app.features.messages.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.GradientHeaderIconContainer
import com.wheels.app.core.ui.theme.GradientHeaderPrimaryContent
import com.wheels.app.core.ui.theme.GradientHeaderSecondaryContent
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.core.ui.theme.gradientHeaderBrush
import com.wheels.app.features.messages.presentation.model.MessageBubbleUiModel
import com.wheels.app.features.messages.presentation.model.MessageThreadUiModel
import com.wheels.app.features.messages.presentation.viewmodel.MessageChatViewModel
import kotlinx.coroutines.launch

@Composable
fun MessageChatScreen(
    navController: NavController,
    viewModel: MessageChatViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val thread = state.thread
    val scope = rememberCoroutineScope()
    var isComposerFocused by remember { mutableStateOf(false) }
    var draftMessage by remember { mutableStateOf("") }
    val composerEnabled = thread?.canSendMessages == true
    val sendEnabled = composerEnabled && state.isOnline

    val baseModifier = Modifier
        .fillMaxSize()
        .background(WheelsBackground)
        .then(if (isComposerFocused) Modifier else Modifier.navigationBarsPadding())
        .imePadding()

    Column(
        modifier = baseModifier
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading messages...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else if (thread != null) {
            MessageChatHeader(
                thread = thread,
                onBack = { navController.popBackStack() }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(thread.messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
            }

            MessageComposer(
                value = draftMessage,
                onValueChanged = { draftMessage = it },
                onFocusChanged = { isComposerFocused = it },
                textEnabled = composerEnabled,
                sendEnabled = sendEnabled,
                onSend = {
                    val messageToSend = draftMessage
                    scope.launch {
                        if (viewModel.sendMessage(messageToSend) && draftMessage == messageToSend) {
                            draftMessage = ""
                        }
                    }
                }
            )
        } else {
            MessageChatHeaderPlaceholder(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MessageChatHeader(
    thread: MessageThreadUiModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(gradientHeaderBrush())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = GradientHeaderIconContainer) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GradientHeaderPrimaryContent
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = WheelsSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = thread.participantInitials,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = SecondaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.participantName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = GradientHeaderPrimaryContent
                )
                Text(
                    text = thread.participantStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = GradientHeaderSecondaryContent
                )
            }
        }
    }
}

@Composable
private fun MessageChatHeaderPlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(gradientHeaderBrush())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Surface(shape = CircleShape, color = GradientHeaderIconContainer) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GradientHeaderPrimaryContent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Conversation unavailable",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = GradientHeaderPrimaryContent
        )
    }
}

@Composable
private fun MessageBubble(message: MessageBubbleUiModel) {
    val isCurrentUser = message.isCurrentUser
    val bubbleColor = if (isCurrentUser) PrimaryBlue else Color(0xFFE8EDF4)
    val textColor = if (isCurrentUser) WheelsSurface else PrimaryBlue
    val timestampColor = if (isCurrentUser) WheelsSurface.copy(alpha = 0.75f) else TextSecondary
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.84f),
            shape = shape,
            color = bubbleColor,
            border = if (isCurrentUser) null else BorderStroke(1.dp, Border),
            shadowElevation = if (isCurrentUser) 4.dp else 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = timestampColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun MessageComposer(
    value: String,
    onValueChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    textEnabled: Boolean,
    sendEnabled: Boolean,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        shape = RoundedCornerShape(22.dp),
        color = WheelsSurface,
        border = BorderStroke(1.dp, Border),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChanged,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                enabled = textEnabled,
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text(
                        text = "Message",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = {
                    if (sendEnabled) {
                        onSend()
                    }
                }),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WheelsSurface,
                    unfocusedContainerColor = WheelsSurface,
                    disabledContainerColor = WheelsSurface,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Border,
                    cursorColor = PrimaryBlue,
                    focusedTextColor = PrimaryBlue,
                    unfocusedTextColor = PrimaryBlue,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextSecondary
                )
            )

            if (sendEnabled && value.isNotBlank()) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = PrimaryBlue
                ) {
                    IconButton(onClick = onSend) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = WheelsSurface
                        )
                    }
                }
            }
        }
    }
}
