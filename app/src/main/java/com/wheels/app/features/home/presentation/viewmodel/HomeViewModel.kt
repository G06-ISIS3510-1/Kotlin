package com.wheels.app.features.home.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.core.location.domain.model.CurrentLocationLabel
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val userDestinationInsightsRepository: UserDestinationInsightsRepository,
    private val currentLocationProvider: CurrentLocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var observedInsightsUserId: String? = null

    init {
        loadCurrentLocation()
        observeCurrentUser()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> Unit
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getUserProfileUseCase()
                .catch { /* Keep fallback name if profile loading fails */ }
                .collect { user ->
                    _uiState.value = _uiState.value.copy(
                        userName = user
                            ?.fullName
                            ?.substringBefore(" ")
                            ?.ifBlank { user.fullName }
                            ?: "User"
                    )

                    if (user == null) {
                        observedInsightsUserId = null
                        _uiState.value = _uiState.value.copy(
                            destinationInsights = emptyList(),
                            trackedDestinationBookings = 0
                        )
                    } else if (observedInsightsUserId != user.id) {
                        observedInsightsUserId = user.id
                        observeDestinationInsights(user.id)
                    }
                }
        }
    }

    private fun observeDestinationInsights(userId: String) {
        viewModelScope.launch {
            userDestinationInsightsRepository.observeUserDestinationInsights(userId)
                .catch {
                    _uiState.value = _uiState.value.copy(
                        destinationInsights = emptyList(),
                        trackedDestinationBookings = 0
                    )
                }
                .collect { insights ->
                    _uiState.value = _uiState.value.copy(
                        destinationInsights = insights?.topDestinations.orEmpty().map {
                            FrequentDestinationUiModel(
                                destinationName = it.destinationName,
                                bookingCount = it.bookingCount,
                                rank = it.rank
                            )
                        },
                        trackedDestinationBookings = insights?.totalBookingsTracked ?: 0
                    )
                }
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            val currentLocation = runCatching { currentLocationProvider.getCurrentLocationLabel() }.getOrNull()
            _uiState.value = _uiState.value.copy(
                currentLocation = currentLocation,
                locationAwareCard = buildLocationAwareCard(currentLocation)
            )
        }
    }

    private fun buildLocationAwareCard(currentLocation: CurrentLocationLabel?): LocationAwareCardUiModel {
        val locationName = currentLocation?.title?.takeIf { it.isNotBlank() }
        val locationSubtitle = currentLocation?.subtitle?.takeIf { it.isNotBlank() }

        return if (locationName != null) {
            LocationAwareCardUiModel(
                title = "Rides near $locationName",
                description = locationSubtitle
                    ?.let { "Your location suggests nearby pickup opportunities around $locationName, $it." }
                    ?: "Your current location suggests nearby pickup opportunities around $locationName.",
                actionLabel = "Explore nearby rides",
                isPreciseLocationAvailable = true
            )
        } else {
            LocationAwareCardUiModel(
                title = "Use your location to find rides faster",
                description = "Once location is available, Wheels can highlight rides closer to your pickup area.",
                actionLabel = "Open rides",
                isPreciseLocationAvailable = false
            )
        }
    }
}

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "User",
    val welcomeMessage: String = "Welcome back",
    val currentLocation: CurrentLocationLabel? = null,
    val locationAwareCard: LocationAwareCardUiModel = LocationAwareCardUiModel(),
    val quickStats: List<HomeQuickStat> = listOf(
        HomeQuickStat(label = "Rides", value = "12"),
        HomeQuickStat(label = "Reliability", value = "98%", accentColor = Color(0xFF10B981)),
        HomeQuickStat(label = "Rating", value = "5.0")
    ),
    val activeRide: ActiveRideUiModel = ActiveRideUiModel(),
    val destinationInsights: List<FrequentDestinationUiModel> = emptyList(),
    val trackedDestinationBookings: Int = 0,
    val updates: List<HomeUpdateUiModel> = listOf(
        HomeUpdateUiModel(
            title = "Driver arriving soon",
            description = "Carlos is 3 minutes away from pickup",
            timestamp = "Now",
            tone = UpdateTone.Success
        ),
        HomeUpdateUiModel(
            title = "You earned punctuality points!",
            description = "+5 points for being on time",
            timestamp = "5m",
            tone = UpdateTone.Info
        )
    )
)

data class FrequentDestinationUiModel(
    val destinationName: String,
    val bookingCount: Int,
    val rank: Int
)

data class LocationAwareCardUiModel(
    val title: String = "Location-aware suggestions",
    val description: String = "Wheels can adapt ride suggestions using your current area.",
    val actionLabel: String = "Open rides",
    val isPreciseLocationAvailable: Boolean = false
)

data class HomeQuickStat(
    val label: String,
    val value: String,
    val accentColor: Color? = null
)

data class ActiveRideUiModel(
    val driver: String = "Carlos Mendez",
    val rating: String = "4.8",
    val carModel: String = "Toyota Corolla 2020",
    val licensePlate: String = "ABC-123",
    val pickupLocation: String = "Campus Uniandes - Entrance Gate",
    val destination: String = "Centro Comercial Andino",
    val etaMinutes: Int = 3,
    val fare: String = "$3,500",
    val distance: String = "4.2 km",
    val routeProgress: Int = 45
)

data class HomeUpdateUiModel(
    val title: String,
    val description: String,
    val timestamp: String,
    val tone: UpdateTone
)

enum class UpdateTone {
    Success,
    Info
}
