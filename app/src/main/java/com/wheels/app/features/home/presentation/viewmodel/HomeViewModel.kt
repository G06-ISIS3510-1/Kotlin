package com.wheels.app.features.home.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.core.location.domain.model.CurrentLocationLabel
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.domain.model.User
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import com.wheels.app.features.rides.domain.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val userDestinationInsightsRepository: UserDestinationInsightsRepository,
    private val rideRepository: RideRepository,
    private val reviewRepository: RideReviewRepository,
    private val currentLocationProvider: CurrentLocationProvider,
    roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val activeRole: StateFlow<UserRole> = roleManager.activeRole
    private var currentUser: User? = null
    private var observedInsightsUserId: String? = null
    private var observedRideUserId: String? = null
    private val dismissedCompletedRideIds = mutableSetOf<String>()
    private var latestPassengerRide: Ride? = null
    private var latestReviewSummaries: Map<String, DriverReviewSummary> = emptyMap()

    init {
        loadCurrentLocation()
        observeCurrentUser()
        observeActiveRole()
        observeReviewSummaries()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> Unit
            HomeEvent.ClearCurrentRide -> clearCurrentRideForDemo()
            HomeEvent.QuickPayCompleted -> completeQuickPayForDemo()
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getUserProfileUseCase()
                .catch { /* Keep fallback name if profile loading fails */ }
                .collect { user ->
                    currentUser = user
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
                    syncPassengerRideObserver()
                }
        }
    }

    private fun observeActiveRole() {
        viewModelScope.launch {
            activeRole.collect {
                syncPassengerRideObserver()
            }
        }
    }

    private fun syncPassengerRideObserver() {
        val user = currentUser
        if (activeRole.value != UserRole.PASSENGER || user == null) {
            observedRideUserId = null
            _uiState.value = _uiState.value.copy(
                currentRide = null,
                showQuickPay = false
            )
            return
        }

        if (observedRideUserId == user.id) return
        observedRideUserId = user.id
        observePassengerRide(user.id)
    }

    private fun observePassengerRide(userId: String) {
        viewModelScope.launch {
            rideRepository.watchCurrentPassengerRide(userId)
                .catch {
                    latestPassengerRide = null
                    renderPassengerRide()
                }
                .collect { ride ->
                    latestPassengerRide = ride
                    renderPassengerRide()
                }
        }
    }

    private fun observeReviewSummaries() {
        viewModelScope.launch {
            reviewRepository.observeDriverReviewSummaries()
                .catch {
                    latestReviewSummaries = emptyMap()
                    renderPassengerRide()
                }
                .collect { summaries ->
                    latestReviewSummaries = summaries
                    renderPassengerRide()
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

    private fun clearCurrentRideForDemo() {
        _uiState.value.currentRide?.rideId?.let { dismissedCompletedRideIds += it }
        _uiState.value = _uiState.value.copy(
            currentRide = null,
            showQuickPay = false
        )
    }

    private fun completeQuickPayForDemo() {
        _uiState.value.currentRide?.rideId?.let { dismissedCompletedRideIds += it }
        _uiState.value = _uiState.value.copy(
            currentRide = null,
            showQuickPay = false
        )
    }

    private fun renderPassengerRide() {
        val ride = latestPassengerRide
        val mappedRide = ride?.toHomeRideUiModel(latestReviewSummaries[ride.driverId])
        val shouldHideForDemo = mappedRide?.rideId != null &&
            mappedRide.rideId in dismissedCompletedRideIds

        _uiState.value = _uiState.value.copy(
            currentRide = if (shouldHideForDemo) null else mappedRide,
            showQuickPay = mappedRide?.status == RideDisplayStatus.COMPLETED && !shouldHideForDemo
        )
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
    data object ClearCurrentRide : HomeEvent
    data object QuickPayCompleted : HomeEvent
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
    val currentRide: HomeRideUiModel? = null,
    val showQuickPay: Boolean = false,
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

data class HomeRideUiModel(
    val rideId: String,
    val driverId: String,
    val driver: String,
    val rating: String,
    val carModel: String,
    val licensePlate: String,
    val pickupLocation: String,
    val destination: String,
    val etaMinutes: Int,
    val fare: String,
    val distance: String,
    val routeProgress: Int,
    val status: RideDisplayStatus
)

enum class RideDisplayStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED
}

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

private fun Ride.toHomeRideUiModel(reviewSummary: DriverReviewSummary? = null): HomeRideUiModel {
    val rideStatus = when (status) {
        "in_progress" -> RideDisplayStatus.IN_PROGRESS
        "completed" -> RideDisplayStatus.COMPLETED
        else -> RideDisplayStatus.OPEN
    }
    val routeProgress = when (rideStatus) {
        RideDisplayStatus.OPEN -> 25
        RideDisplayStatus.IN_PROGRESS -> 60
        RideDisplayStatus.COMPLETED -> 100
    }
    val etaMinutes = when (rideStatus) {
        RideDisplayStatus.COMPLETED -> 0
        RideDisplayStatus.IN_PROGRESS -> 3
        RideDisplayStatus.OPEN -> estimatedDurationMinutes
    }
    val displayRating = reviewSummary?.displayRating ?: 0.0

    return HomeRideUiModel(
        rideId = id,
        driverId = driverId,
        driver = driverName.ifBlank { "Uniandes driver" },
        rating = String.format("%.1f", displayRating),
        carModel = carModel.ifBlank { "Vehicle info pending" },
        licensePlate = licensePlate.ifBlank { "Available later" },
        pickupLocation = origin,
        destination = destination,
        etaMinutes = etaMinutes,
        fare = "$" + String.format("%,d", pricePerSeat.toInt()),
        distance = when (rideStatus) {
            RideDisplayStatus.COMPLETED -> "Completed"
            RideDisplayStatus.IN_PROGRESS -> "On the way"
            RideDisplayStatus.OPEN -> "Ready"
        },
        routeProgress = routeProgress,
        status = rideStatus
    )
}
