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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.core.ui.theme.gradientHeaderBrush
import com.wheels.app.features.rides.domain.model.CreateRideDraftSummary
import com.wheels.app.features.rides.presentation.viewmodel.CreateRideDraftsEvent
import com.wheels.app.features.rides.presentation.viewmodel.CreateRideDraftsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateRideDraftsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: CreateRideDraftsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete saved draft?") },
            text = { Text("This will remove the local Create Ride draft from your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.onEvent(CreateRideDraftsEvent.DeleteDraft)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DraftsHeader(onBack = {
                if (!navController.popBackStack()) {
                    navController.navigate(Destinations.Rides.route)
                }
            })

            if (activeRole != UserRole.DRIVER) {
                DraftEmptyState(
                    title = "Driver access only",
                    message = "Saved ride drafts are only available for drivers.",
                    actionLabel = "Go back",
                    onAction = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Destinations.Rides.route)
                        }
                    }
                )
                return@Surface
            }

            if (!state.isOnline) {
                OfflineDraftBanner()
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.draft == null -> {
                    DraftEmptyState(
                        title = "No saved ride draft",
                        message = "You do not have any locally stored Create Ride draft yet.",
                        actionLabel = "Go to Create Ride",
                        onAction = {
                            navController.navigate(Destinations.Rides.route) {
                                popUpTo(Destinations.Rides.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                else -> {
                    val draft = state.draft ?: return@Surface
                    DraftSummaryCard(
                        draft = draft,
                        isDeleting = state.isDeleting,
                        onContinueEditing = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Destinations.Rides.route) {
                                    popUpTo(Destinations.Rides.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onDeleteDraft = { showDeleteConfirmation = true }
                    )
                }
            }

            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB3261E)
                )
            }
        }
    }
}

@Composable
private fun DraftsHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradientHeaderBrush())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = "Saved Ride Draft",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Continue your Create Ride flow even without internet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
        }
    }
}

@Composable
private fun OfflineDraftBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        color = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "This draft is available offline because it is stored locally on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun DraftSummaryCard(
    draft: CreateRideDraftSummary,
    isDeleting: Boolean,
    onContinueEditing: () -> Unit,
    onDeleteDraft: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = WheelsSurface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Stored locally",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Last updated ${draft.updatedAtMillis.toRelativeDraftDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            DraftDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Route",
                value = "${draft.origin.ifBlank { "No pickup yet" }} → ${draft.destination.ifBlank { "No destination yet" }}"
            )
            DraftDetailRow(
                icon = Icons.Default.Schedule,
                label = "Schedule",
                value = buildString {
                    append(draft.date.ifBlank { "No date" })
                    append(" • ")
                    append(draft.time.ifBlank { "No time" })
                }
            )
            DraftDetailRow(
                icon = Icons.Default.DirectionsCar,
                label = "Vehicle",
                value = buildString {
                    append(draft.carModel.ifBlank { "No car model" })
                    append(" • ")
                    append(draft.licensePlate.ifBlank { "No plate" })
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DraftMetaChip(text = "${draft.totalSeats} seats")
                DraftMetaChip(text = "${draft.pricePerSeat.ifBlank { "0" }} COP/seat")
                if (draft.usedCurrentLocationOrigin || draft.usedCurrentLocationDestination) {
                    DraftMetaChip(text = "Uses current location")
                }
            }

            if (draft.description.isNotBlank()) {
                Text(
                    text = draft.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onContinueEditing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Continue Editing")
                }
                OutlinedButton(
                    onClick = onDeleteDraft,
                    enabled = !isDeleting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete Draft")
                }
            }
        }
    }
}

@Composable
private fun DraftDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DraftMetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFEAF1FB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryBlue
        )
    }
}

@Composable
private fun DraftEmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        color = WheelsSurface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = PrimaryBlue
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

private fun Long.toRelativeDraftDate(): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.US)
    return formatter.format(Date(this))
}
