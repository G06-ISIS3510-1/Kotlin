package com.wheels.app.core.analytics.bqt3.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BQT3WeeklyUsageSection(
    userId: String,
    viewModel: BQT3ViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage
    val emptyStateMessage = state.emptyStateMessage

    LaunchedEffect(userId) {
        viewModel.loadWeeklyUsage(userId)
        viewModel.syncPendingEvents()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Rides Near Me this week",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            state.isLoading -> {
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Checking weekly usage...")
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            emptyStateMessage != null -> {
                Text(
                    text = emptyStateMessage,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                Text(
                    text = "${state.usageCount} use(s)",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                state.lastUpdatedLabel?.let { label ->
                    Text(
                        text = label + if (state.isFromCache) " · Cached" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.registerRidesNearMeUsage(userId) },
            modifier = Modifier.padding(top = 12.dp),
            enabled = !state.isLoading
        ) {
            Text(text = "Track Rides Near Me usage")
        }
    }
}
