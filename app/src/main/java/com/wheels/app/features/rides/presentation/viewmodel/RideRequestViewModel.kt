package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideRequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val rideRepository: RideRepository,
    private val userDestinationInsightsRepository: UserDestinationInsightsRepository
) : ViewModel() {

    private val rideId: String = savedStateHandle.get<String>("rideId").orEmpty()

    private val _uiState = MutableStateFlow(
        RideRequestUiState(
            ride = null,
            selectedSeats = 1,
            showConfirmation = false,
            isLoading = true
        )
    )
    val uiState: StateFlow<RideRequestUiState> = _uiState.asStateFlow()

    init {
        observeRide()
    }

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
                confirmRideRequest()
            }
        }
    }

    private fun confirmRideRequest() {
        val currentRide = _uiState.value.ride ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showConfirmation = false,
                    isApplyingRequest = true,
                    requestErrorMessage = null
                )
            }

            val currentUser = getUserProfileUseCase().firstOrNull()
            if (currentUser == null) {
                _uiState.update {
                    it.copy(
                        isApplyingRequest = false,
                        requestErrorMessage = "We could not load your profile right now."
                    )
                }
                return@launch
            }

            runCatching {
                rideRepository.applyToRide(
                    rideId = currentRide.id,
                    passengerId = currentUser.id,
                    passengerName = currentUser.fullName.ifBlank {
                        currentUser.email.substringBefore("@")
                    },
                    passengerEmail = currentUser.email
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isApplyingRequest = false,
                        requestConfirmed = true,
                        requestErrorMessage = null
                    )
                }

                runCatching {
                    userDestinationInsightsRepository.logRideBookedDestination(
                        userId = currentUser.id,
                        rideId = currentRide.id,
                        destinationName = currentRide.destination
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isApplyingRequest = false,
                        requestConfirmed = false,
                        requestErrorMessage = throwable.message
                            ?: "We could not apply to this ride right now."
                    )
                }
            }
        }
    }

    private fun observeRide() {
        viewModelScope.launch {
            rideRepository.observeRide(rideId)
                .catch {
                    _uiState.update { it.copy(ride = null, isLoading = false) }
                }
                .collect { ride ->
                    _uiState.update { state ->
                        state.copy(
                            ride = ride?.toRideRequestUiModel(),
                            isLoading = false
                        )
                    }
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
    val requestConfirmed: Boolean = false,
    val isApplyingRequest: Boolean = false,
    val requestErrorMessage: String? = null,
    val isLoading: Boolean = false
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

private fun Ride.toRideRequestUiModel(): RideRequestUiModel {
    val departureDateTime = departureTime.atZone(ZoneId.systemDefault())
    val arrivalDateTime = departureTime
        .plusSeconds(estimatedDurationMinutes * 60L)
        .atZone(ZoneId.systemDefault())

    return RideRequestUiModel(
        id = id,
        origin = origin,
        destination = destination,
        departureTime = departureDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        departureDate = departureDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
        estimatedDuration = "$estimatedDurationMinutes min",
        estimatedArrival = arrivalDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        price = pricePerSeat.toInt(),
        availableSeats = availableSeats,
        totalSeats = totalSeats,
        distance = "$estimatedDurationMinutes min route",
        route = listOf(origin, destination),
        amenities = buildList {
            if (verifiedByUniversity) add("University verified")
            if (carModel.isNotBlank()) add(carModel)
        },
        cancellationPolicy = "Please cancel as early as possible so other students can reorganize in time.",
        driver = RideDriverUiModel(
            name = driverName.ifBlank { "Uniandes driver" },
            rating = driverRating,
            ridesCount = reviewCount,
            reliabilityScore = reliabilityScore,
            memberSince = "Uniandes community member",
            carModel = carModel.ifBlank { "Vehicle information available after confirmation" },
            carColor = "Shared in-app",
            licensePlate = licensePlate.ifBlank { "Available after confirmation" },
            punctualityRate = punctualityRate,
            responseTime = "Usually replies quickly"
        )
    )
}
