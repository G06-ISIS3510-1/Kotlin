package com.wheels.app.features.chat.presentation.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.chat.presentation.viewmodel.ChatMessageUiModel
import com.wheels.app.features.chat.presentation.viewmodel.GroupChatEvent
import com.wheels.app.features.chat.presentation.viewmodel.GroupChatUiState
import com.wheels.app.features.chat.presentation.viewmodel.GroupChatViewModel

@Composable
fun GroupChatScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: GroupChatViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
            .navigationBarsPadding()
            .imePadding()
    ) {
        GroupChatTopBar(
            state = state,
            onBack = { navController.popBackStack() },
            onClose = { navController.popBackStack() }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
        }

        MessageComposer(
            value = state.draftMessage,
            onValueChanged = { viewModel.onEvent(GroupChatEvent.DraftChanged(it)) },
            onSend = { viewModel.onEvent(GroupChatEvent.SendMessage) }
        )
    }
}

@Composable
private fun GroupChatTopBar(
    state: GroupChatUiState,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = WheelsSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryBlue
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Text(
                    text = "${state.tripLabel} · ${state.participantsLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageUiModel) {
    val alignment = if (message.isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isCurrentUser) PrimaryBlue else WheelsSurface
    val contentColor = if (message.isCurrentUser) WheelsSurface else PrimaryBlue

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.82f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = if (message.isCurrentUser) null else BorderStroke(1.dp, Border)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (message.isCurrentUser) WheelsSurface.copy(alpha = 0.82f) else SecondaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isCurrentUser) WheelsSurface.copy(alpha = 0.72f) else TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun MessageComposer(
    value: String,
    onValueChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = WheelsSurface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Escribe un mensaje al grupo")
                },
                shape = RoundedCornerShape(18.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.size(12.dp))

            Surface(
                modifier = Modifier.size(52.dp),
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
