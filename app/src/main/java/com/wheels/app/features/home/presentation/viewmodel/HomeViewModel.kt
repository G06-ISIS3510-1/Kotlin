package com.wheels.app.features.home.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.core.location.domain.model.CurrentLocationLabel
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.reviews.data.local.PendingRideReviewEntity
import com.wheels.app.features.reviews.data.local.PendingRideReviewLocalStore
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

/**
 * Owns the Home screen state and also listens for review queue updates.
 *
 * When queued reviews disappear from local storage, Home shows a short success banner so the user
 * gets a visible confirmation that the review eventually reached the server.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val userDestinationInsightsRepository: UserDestinationInsightsRepository,
    private val rideRepository: RideRepository,
    private val reviewRepository: RideReviewRepository,
    private val pendingRideReviewStore: PendingRideReviewLocalStore,
    private val currentLocationProvider: CurrentLocationProvider,
    private val networkMonitor: NetworkMonitor,
    roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val activeRole: StateFlow<UserRole> = roleManager.activeRole
    private var currentUser: User? = null
    private var observedInsightsUserId: String? = null
    private var observedRideUserId: String? = null
    private var observedPendingReviewUserId: String? = null
    private var latestPassengerRide: Ride? = null
    private var latestReviewSummaries: Map<String, DriverReviewSummary> = emptyMap()
    // Keep a lightweight observer on the pending review queue so Home can announce when sync completes.
    private var latestPendingReviews: List<PendingRideReviewEntity> = emptyList()
    private var pendingReviewObserverJob: Job? = null

    init {
        loadCurrentLocation()
        observeCurrentUser()
        observeActiveRole()
        observeReviewSummaries()
        observeConnectivity()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> Unit
            HomeEvent.ClearCurrentRide -> clearCurrentRide()
            HomeEvent.QuickPayCompleted -> completeQuickPay()
            is HomeEvent.ReviewNoticeReceived -> setReviewNotice(event.notice)
            HomeEvent.ClearReviewNotice -> _uiState.value = _uiState.value.copy(
                reviewNotice = null
            )
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
                            trackedDestinationBookings = 0,
                            reviewNotice = null
                        )
                        syncPendingReviewObserver()
                    } else if (observedInsightsUserId != user.id) {
                        observedInsightsUserId = user.id
                        observeDestinationInsights(user.id)
                        syncPendingReviewObserver()
                    } else {
                        syncPendingReviewObserver()
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
                        trackedDestinationBookings = 0,
                        destinationInsightsLastUpdatedLabel = null
                    )
                }
                .collect { insights ->
                    val lastUpdatedLabel = insights?.lastUpdatedMillis?.let(::formatLastUpdatedLabel)
                    _uiState.value = _uiState.value.copy(
                        destinationInsights = insights?.topDestinations.orEmpty().map {
                            FrequentDestinationUiModel(
                                destinationName = it.destinationName,
                                bookingCount = it.bookingCount,
                                rank = it.rank
                            )
                        },
                        trackedDestinationBookings = insights?.totalBookingsTracked ?: 0,
                        destinationInsightsLastUpdatedLabel = lastUpdatedLabel
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

    private fun clearCurrentRide() {
        _uiState.value = _uiState.value.copy(
            currentRide = null,
            showQuickPay = false
        )
    }

    private fun completeQuickPay() {
        val ride = _uiState.value.currentRide
        val user = currentUser
        _uiState.value = _uiState.value.copy(
            currentRide = null,
            showQuickPay = false
        )

        if (ride == null || user == null) {
            return
        }

        viewModelScope.launch {
            runCatching {
                rideRepository.dismissPassengerRide(
                    rideId = ride.rideId,
                    passengerId = user.id
                )
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.observeIsOnline()
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline && _uiState.value.reviewNotice?.dismissOnReconnect == true) {
                        _uiState.value = _uiState.value.copy(reviewNotice = null)
                    }
                }
        }
    }

    private fun syncPendingReviewObserver() {
        val userId = currentUser?.id
        if (userId.isNullOrBlank()) {
            observedPendingReviewUserId = null
            latestPendingReviews = emptyList()
            pendingReviewObserverJob?.cancel()
            pendingReviewObserverJob = null
            _uiState.value = _uiState.value.copy(reviewNotice = null)
            return
        }

        if (observedPendingReviewUserId == userId && pendingReviewObserverJob?.isActive == true) return

        observedPendingReviewUserId = userId
        latestPendingReviews = emptyList()
        pendingReviewObserverJob?.cancel()
        _uiState.value = _uiState.value.copy(reviewNotice = null)
        pendingReviewObserverJob = viewModelScope.launch {
            // The queue flow is the source of truth for whether a review is still waiting locally.
            // We keep this collection scoped to the current user so one rider never sees another's queue state.
            reviewQueueMessages(userId)
        }
    }

    private suspend fun reviewQueueMessages(userId: String) {
        pendingRideReviewStore.observePendingRideReviews()
            .catch {
                // Queue notices should not block the rest of the home screen.
            }
            .collect { pendingReviews ->
                // Filter by the current user so one passenger's offline queue does not affect another's UI.
                val currentPendingReviews = pendingReviews.filter { it.passengerId == userId }
                val previousPendingReviews = latestPendingReviews
                latestPendingReviews = currentPendingReviews

                if (previousPendingReviews.isNotEmpty() && currentPendingReviews.isEmpty()) {
                    // Once the queue drains, show a short success banner and then clear it on the UI.
                    _uiState.value = _uiState.value.copy(
                        reviewNotice = HomeNoticeUiModel(
                            message = buildReviewSyncedMessage(previousPendingReviews),
                            isSuccess = true
                        )
                    )
                }
            }
    }

    private fun buildReviewSyncedMessage(previousPendingReviews: List<PendingRideReviewEntity>): String {
        // Keep this copy short because the banner is temporary.
        return when {
            previousPendingReviews.size > 1 -> "Your reviews were successfully sent."
            else -> "Your review was successfully sent."
        }
    }

    private fun renderPassengerRide() {
        val ride = latestPassengerRide
        val mappedRide = ride?.toHomeRideUiModel(latestReviewSummaries[ride.driverId])

        _uiState.value = _uiState.value.copy(
            currentRide = mappedRide,
            showQuickPay = mappedRide?.status == RideDisplayStatus.COMPLETED
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

    private fun setReviewNotice(notice: HomeNoticeUiModel) {
        // Offline handoff messages should not linger once the device is already back online.
        if (notice.dismissOnReconnect && networkMonitor.isOnline()) {
            return
        }

        _uiState.value = _uiState.value.copy(reviewNotice = notice)
    }

    private fun formatLastUpdatedLabel(timestampMillis: Long): String {
        return "Last updated: ${DateFormat.getDateTimeInstance().format(Date(timestampMillis))}"
    }
}

sealed interface HomeEvent {
    data object Refresh : HomeEvent
    data object ClearCurrentRide : HomeEvent
    data object QuickPayCompleted : HomeEvent
    data class ReviewNoticeReceived(val notice: HomeNoticeUiModel) : HomeEvent
    data object ClearReviewNotice : HomeEvent
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
    val destinationInsightsLastUpdatedLabel: String? = null,
    val reviewNotice: HomeNoticeUiModel? = null,
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

data class HomeNoticeUiModel(
    val message: String,
    val isSuccess: Boolean,
    val dismissOnReconnect: Boolean = false
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
