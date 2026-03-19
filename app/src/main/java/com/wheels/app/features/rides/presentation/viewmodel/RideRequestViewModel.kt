package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RideRequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideId: String = savedStateHandle.get<String>("rideId").orEmpty()
    private val ride = mockRideRequestData[rideId]

    private val _uiState = MutableStateFlow(
        RideRequestUiState(
            ride = ride,
            selectedSeats = 1,
            showConfirmation = false
        )
    )
    val uiState: StateFlow<RideRequestUiState> = _uiState.asStateFlow()

    fun onEvent(event: RideRequestEvent) {
        when (event) {
            is RideRequestEvent.SeatSelected -> {
                _uiState.update { it.copy(selectedSeats = event.seats) }
            }

            RideRequestEvent.RequestTapped -> {
                _uiState.update { it.copy(showConfirmation = true) }
            }

            RideRequestEvent.ConfirmationDismissed -> {
                _uiState.update { it.copy(showConfirmation = false) }
            }

            RideRequestEvent.ConfirmRequest -> {
                _uiState.update { it.copy(showConfirmation = false, requestConfirmed = true) }
            }
        }
    }
}

sealed interface RideRequestEvent {
    data class SeatSelected(val seats: Int) : RideRequestEvent
    data object RequestTapped : RideRequestEvent
    data object ConfirmationDismissed : RideRequestEvent
    data object ConfirmRequest : RideRequestEvent
}

data class RideRequestUiState(
    val ride: RideRequestUiModel? = null,
    val selectedSeats: Int = 1,
    val showConfirmation: Boolean = false,
    val requestConfirmed: Boolean = false
) {
    val totalPrice: Int
        get() = (ride?.price ?: 0) * selectedSeats
}

data class RideRequestUiModel(
    val id: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val departureDate: String,
    val estimatedDuration: String,
    val estimatedArrival: String,
    val price: Int,
    val availableSeats: Int,
    val totalSeats: Int,
    val distance: String,
    val route: List<String>,
    val amenities: List<String>,
    val cancellationPolicy: String,
    val driver: RideDriverUiModel
)

data class RideDriverUiModel(
    val name: String,
    val rating: Double,
    val ridesCount: Int,
    val reliabilityScore: Int,
    val memberSince: String,
    val carModel: String,
    val carColor: String,
    val licensePlate: String,
    val punctualityRate: Int,
    val responseTime: String
) {
    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
