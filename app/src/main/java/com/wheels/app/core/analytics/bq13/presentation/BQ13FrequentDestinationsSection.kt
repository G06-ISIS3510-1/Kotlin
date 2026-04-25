package com.wheels.app.core.analytics.bq13.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun BQ13FrequentDestinationsSection(
    userId: String,
    viewModel: BQ13ViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage
    val emptyStateMessage = state.emptyStateMessage

    LaunchedEffect(userId) {
        viewModel.loadFrequentDestinations(userId)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Frequent destinations",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            state.isLoading -> {
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Loading frequent destinations...")
                }
            }

            state.destinations.isNotEmpty() -> {
                val label = buildString {
                    state.lastUpdatedLabel?.let { append(it) }
                    if (state.isFromCache) {
                        if (isNotBlank()) append(" · ")
                        append("Cached")
                    }
                }
                if (label.isNotBlank()) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                state.destinations.forEach { destination ->
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = destination.destinationName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${destination.count} trip(s)" +
                                (destination.coordinatesLabel?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
        }
    }
}
