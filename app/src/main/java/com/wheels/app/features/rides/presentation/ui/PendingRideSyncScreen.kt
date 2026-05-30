package com.wheels.app.features.rides.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.core.ui.theme.gradientHeaderBrush
import com.wheels.app.features.rides.domain.model.PendingRidePublish
import com.wheels.app.features.rides.presentation.viewmodel.PendingRideSyncEvent
import com.wheels.app.features.rides.presentation.viewmodel.PendingRideSyncUiState
import com.wheels.app.features.rides.presentation.viewmodel.PendingRideSyncViewModel
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun PendingRideSyncScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: PendingRideSyncViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    var pendingRemovalId by remember { mutableStateOf<String?>(null) }

    pendingRemovalId?.let { publishId ->
        AlertDialog(
            onDismissRequest = { pendingRemovalId = null },
            title = { Text("Remove pending publish?") },
            text = { Text("This ride has not been published yet. Removing it will discard the local publish request.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRemovalId = null
                        viewModel.onEvent(PendingRideSyncEvent.RemovePublish(publishId))
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRemovalId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding),
        color = WheelsBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PendingSyncHeader(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Destinations.Rides.route)
                    }
                }
            )

            if (activeRole != UserRole.DRIVER) {
                PendingSyncEmptyState(
                    title = "Driver access only",
                    message = "Pending ride synchronization is only available for drivers.",
                    actionLabel = "Go back",
                    onAction = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Destinations.Rides.route)
                        }
                    }
                )
                return@Surface
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    PendingSyncSummaryCard(
                        state = state,
                        onRetryAll = { viewModel.onEvent(PendingRideSyncEvent.RetryAll) }
                    )
                }

                item {
                    PendingSyncConnectivityBanner(isOnline = state.isOnline)
                }

                state.infoMessage?.let { message ->
                    item {
                        PendingSyncMessageCard(
                            message = message,
                            backgroundColor = Color(0xFFE8F0F9),
                            onDismiss = { viewModel.onEvent(PendingRideSyncEvent.DismissInfo) }
                        )
                    }
                }

                state.errorMessage?.let { message ->
                    item {
                        PendingSyncMessageCard(
                            message = message,
                            backgroundColor = Color(0xFFFDECEC),
                            onDismiss = { viewModel.onEvent(PendingRideSyncEvent.DismissError) }
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    state.pendingPublishes.isEmpty() -> {
                        item {
                            PendingSyncEmptyState(
                                title = "No pending ride publishes",
                                message = "Your local queue is empty. Published rides will appear in My Rides instead.",
                                actionLabel = "Go to My Rides",
                                onAction = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate(Destinations.Rides.route)
                                    }
                                }
                            )
                        }
                    }

                    else -> {
                        items(state.pendingPublishes, key = { it.id }) { publish ->
                            PendingRidePublishCard(
                                publish = publish,
                                isRetrying = state.retryingPublishId == publish.id,
                                isRemoving = state.removingPublishId == publish.id,
                                onRetry = {
                                    viewModel.onEvent(PendingRideSyncEvent.RetryPublish(publish.id))
                                },
                                onRemove = { pendingRemovalId = publish.id },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingSyncHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradientHeaderBrush())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Pending Ride Sync",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Review and manage ride publishes still stored locally.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
        }
    }
}

@Composable
private fun PendingSyncSummaryCard(
    state: PendingRideSyncUiState,
    onRetryAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Local Sync Queue",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pending publishes: ${state.pendingPublishes.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Failed publishes: ${state.failedPublishesCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (state.isOnline) "Connection status: Online" else "Connection status: Offline",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onRetryAll,
                enabled = state.pendingPublishes.isNotEmpty() && !state.isRetryingAll,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isRetryingAll) "Retrying..." else "Retry All")
            }
        }
    }
}

@Composable
private fun PendingSyncConnectivityBanner(isOnline: Boolean) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFFE8F0F9) else Color(0xFFFEF3C7)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isOnline) PrimaryBlue else Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isOnline) {
                    "You are online. Pending rides can be retried manually or synced automatically."
                } else {
                    "You are offline. Pending rides will remain in local storage until your connection returns."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun PendingSyncMessageCard(
    message: String,
    backgroundColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun PendingRidePublishCard(
    publish: PendingRidePublish,
    isRetrying: Boolean,
    isRemoving: Boolean,
    onRetry: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val departureAt = publish.request.departureAt.atZone(ZoneId.systemDefault())
    val departureDate = departureAt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val departureTime = departureAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    val statusLabel = if (publish.lastError.isNullOrBlank()) "PENDING" else "FAILED"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "${publish.request.origin} -> ${publish.request.destination}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$departureDate • $departureTime",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${publish.request.totalSeats} seats • \$${publish.request.pricePerSeat} • ${publish.request.carModel}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: $statusLabel",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (statusLabel == "FAILED") Color(0xFFB3261E) else PrimaryBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Retry count: ${publish.retryCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Saved locally: ${formatPendingPublishTimestamp(publish.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            publish.lastError?.takeIf { it.isNotBlank() }?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last error: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB3261E)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRetry,
                    enabled = !isRetrying,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isRetrying) "Retrying..." else "Retry")
                }
                OutlinedButton(
                    onClick = onRemove,
                    enabled = !isRemoving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isRemoving) "Removing..." else "Remove")
                }
            }
        }
    }
}

@Composable
private fun PendingSyncEmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(14.dp)) {
                Text(actionLabel)
            }
        }
    }
}

private fun formatPendingPublishTimestamp(timestampMillis: Long): String {
    return runCatching {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestampMillis))
    }.getOrDefault("recently")
}
