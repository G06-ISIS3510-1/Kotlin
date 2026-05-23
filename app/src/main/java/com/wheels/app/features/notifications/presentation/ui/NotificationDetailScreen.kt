package com.wheels.app.features.notifications.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationDetailScreen(innerPadding: PaddingValues = PaddingValues(), notification: NotificationUiModel = NotificationUiModel("preview", "Driver payout ready", "This detail view will later resolve cached Room content.", "Just now", false), syncState: String = "Idle", onMarkAsRead: (String) -> Unit = {}, onDeleteNotification: (String) -> Unit = {}) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
        Text(notification.title, style = MaterialTheme.typography.headlineSmall)
        Text(notification.timestampLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Text(notification.body)
        Text("State: $syncState")
        Text("Local-first detail state can survive rotation; WorkManager can later replay pending_notification_actions after reconnect.")
        Text("Future markAsRead/delete flows will run in viewModelScope and Dispatchers.IO, then reconcile remote and local inbox copies.")
    } }
}
