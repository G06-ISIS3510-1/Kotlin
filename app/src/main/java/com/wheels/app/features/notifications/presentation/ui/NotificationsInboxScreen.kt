package com.wheels.app.features.notifications.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class NotificationUiModel(val id: String, val title: String, val body: String, val timestampLabel: String, val isRead: Boolean)
private val demoNotifications = listOf(NotificationUiModel("1", "Trip update", "Your ride request was confirmed.", "2m ago", false), NotificationUiModel("2", "Payment receipt", "Receipt stored for offline review.", "1h ago", true))

@Composable
fun NotificationsInboxScreen(innerPadding: PaddingValues = PaddingValues(), notifications: List<NotificationUiModel> = demoNotifications, isLoading: Boolean = false, errorMessage: String? = null, onRefresh: () -> Unit = {}, onMarkAsRead: (String) -> Unit = {}, onDeleteNotification: (String) -> Unit = {}, onOpenNotification: (NotificationUiModel) -> Unit = {}) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)
        Text("Coroutines + StateFlow will push inbox updates reactively; IO, WorkManager, and queued actions stay off the main thread.")
        Text("Offline-first browsing can reuse local Room data until connectivity returns and reconciliation runs.")
        if (isLoading) Text("Syncing inbox...")
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(notifications, key = { it.id }) { n ->
            Card(onClick = { onOpenNotification(n) }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(n.title, style = MaterialTheme.typography.titleMedium); Text(n.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${n.timestampLabel} - ${if (n.isRead) "read" else "unread"}")
            } }
        } }
    } }
}
