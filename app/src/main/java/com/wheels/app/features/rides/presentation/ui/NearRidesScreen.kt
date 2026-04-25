package com.wheels.app.features.rides.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wheels.app.features.rides.presentation.viewmodel.NearRideCardUiModel
import com.wheels.app.features.rides.presentation.viewmodel.NearRidesViewModel

@Composable
fun NearRidesScreen(
    viewModel: NearRidesViewModel,
    onRideSelected: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNearRides()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.destinationQuery,
            onValueChange = viewModel::loadNearRides,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Destination") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.loadNearRides() },
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Loading..." else "Refresh near rides")
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            items(state.rides, key = { it.id }) { ride ->
                NearRideCard(
                    ride = ride,
                    onClick = { onRideSelected(ride.id) }
                )
            }
        }
    }
}

@Composable
private fun NearRideCard(
    ride: NearRideCardUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ride.destination, style = MaterialTheme.typography.titleMedium)
            Text(text = "From ${ride.origin}", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(text = ride.departureTime)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "$${ride.price} · ${ride.availableSeats} seats")
            }
        }
    }
}
