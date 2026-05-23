package com.wheels.app.features.notifications.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class NotificationUiModel(
    val id: String,
    val title: String,
    val body: String,
    val timestampLabel: String,
    val isRead: Boolean
)

private val demoNotifications = listOf(
    NotificationUiModel("1", "Trip update", "Your ride request was confirmed.", "2m ago", false),
    NotificationUiModel("2", "Payment receipt", "Receipt stored for offline review.", "1h ago", true)
)

@Composable
fun NotificationsInboxScreen(
    innerPadding: PaddingValues = PaddingValues(),
    notifications: List<NotificationUiModel> = demoNotifications,
    onOpenNotification: (NotificationUiModel) -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium)
            Text("Primitive inbox for Room history, pending actions, and cached content.")
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications, key = { it.id }) { notification ->
                    Card(onClick = { onOpenNotification(notification) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(notification.title, style = MaterialTheme.typography.titleMedium)
                            Text(notification.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${notification.timestampLabel} - ${if (notification.isRead) "read" else "unread"}")
                        }
                    }
                }
            }
        }
    }
}
