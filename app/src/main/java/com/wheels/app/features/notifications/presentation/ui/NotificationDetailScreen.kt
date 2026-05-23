package com.wheels.app.features.notifications.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationDetailScreen(
    innerPadding: PaddingValues = PaddingValues(),
    notification: NotificationUiModel = NotificationUiModel(
        id = "preview",
        title = "Driver payout ready",
        body = "This detail view will later resolve full cached content from Room.",
        timestampLabel = "Just now",
        isRead = false
    )
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(notification.title, style = MaterialTheme.typography.headlineSmall)
            Text(notification.timestampLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(notification.body)
            Spacer(Modifier.height(12.dp))
            Text("Offline path: Room notification metadata, pending actions, and Proto preferences.")
            Text("Caching path: in-memory snapshots plus image caches, with state kept through quick rotations.")
            Text("Analytics path: future BQ-R2 events can track detail opens and cleanup actions.")
        }
    }
}
