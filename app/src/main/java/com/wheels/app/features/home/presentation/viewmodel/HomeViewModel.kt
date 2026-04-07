package com.wheels.app.features.home.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
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
    private val userDestinationInsightsRepository: UserDestinationInsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var observedInsightsUserId: String? = null

    init {
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
}

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "User",
    val welcomeMessage: String = "Welcome back",
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
