package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.trust.domain.model.TrustScoreNotice
import com.wheels.app.core.trust.domain.model.TrustScoreNoticeType
import com.wheels.app.core.trust.domain.repository.DriverRideTrustActionParams
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.rides.domain.usecase.GetAvailableRidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val getAvailableRidesUseCase: GetAvailableRidesUseCase,
    private val authRepository: AuthRepository,
    private val driverTrustRepository: DriverTrustRepository,
    roleManager: RoleManager
) : ViewModel() {

    private var currentDriverId: String? = null
    private var observedTrustUserId: String? = null

    private val mockRides = listOf(
        RideCardUiModel(
            id = "1",
            driver = "Carlos Mendez",
            rating = 4.8,
            ridesCount = 124,
            reliabilityScore = 98,
            origin = "Campus Uniandes",
            destination = "Centro Comercial Andino",
            destinationArea = "Chapinero",
            departureTime = "14:30",
            estimatedDuration = "30 min",
            price = 3500,
            availableSeats = 3,
            totalSeats = 4,
            isHabitRide = true,
            punctualityRate = 96
        ),
        RideCardUiModel(
            id = "2",
            driver = "Maria Sanchez",
            rating = 4.9,
            ridesCount = 89,
            reliabilityScore = 99,
            origin = "Campus Uniandes",
            destination = "Usaquen",
            destinationArea = "Usaquen",
            departureTime = "15:00",
            estimatedDuration = "35 min",
            price = 4000,
            availableSeats = 2,
            totalSeats = 3,
            isHabitRide = false,
            punctualityRate = 98
        ),
        RideCardUiModel(
            id = "3",
            driver = "Juan Pablo",
            rating = 4.7,
            ridesCount = 67,
            reliabilityScore = 94,
            origin = "Campus Uniandes",
            destination = "Suba",
            destinationArea = "Suba",
            departureTime = "15:30",
            estimatedDuration = "45 min",
            price = 5000,
            availableSeats = 1,
            totalSeats = 4,
            isHabitRide = false,
            punctualityRate = 92
        )
    )

    private val mockDriverRides = listOf(
        DriverRideUiModel(
            id = "driver-1",
            origin = "Campus Uniandes - Main Gate",
            destination = "Centro Comercial Andino",
            date = "2026-03-19",
            time = "14:30",
            estimatedArrival = "15:00",
            totalSeats = 3,
            pricePerSeat = 3500,
            carModel = "Toyota Corolla 2020",
            licensePlate = "ABC-123",
            status = DriverRideStatus.ACTIVE,
            passengers = defaultPassengers
        ),
        DriverRideUiModel(
            id = "driver-2",
            origin = "Campus Uniandes - ML Building",
            destination = "Usaquen",
            date = "2026-03-19",
            time = "17:15",
            estimatedArrival = "17:50",
            totalSeats = 2,
            pricePerSeat = 4000,
            carModel = "Mazda 3 2021",
            licensePlate = "XYZ-456",
            status = DriverRideStatus.PENDING,
            passengers = defaultPassengers.take(2).map { it.copy(paymentStatus = PaymentStatusState.PENDING) }
        ),
        DriverRideUiModel(
            id = "driver-3",
            origin = "Campus Uniandes - Entrance Gate",
            destination = "Suba Centro",
            date = "2026-03-20",
            time = "07:45",
            estimatedArrival = "08:25",
            totalSeats = 4,
            pricePerSeat = 4500,
            carModel = "Chevrolet Spark 2019",
            licensePlate = "DEF-789",
            status = DriverRideStatus.COMPLETED,
            passengers = defaultPassengers.mapIndexed { index, passenger ->
                passenger.copy(
                    paymentStatus = if (index == 2) PaymentStatusState.PENDING else PaymentStatusState.PAID
                )
            }
        )
    ).sortedBy { "${it.date} ${it.time}" }

    private val _uiState = MutableStateFlow(
        RidesUiState(
            allRides = mockRides,
            filteredRides = mockRides,
            driverRides = mockDriverRides
        )
    )
    val uiState: StateFlow<RidesUiState> = _uiState.asStateFlow()
    val activeRole: StateFlow<UserRole> = roleManager.activeRole

    init {
        observeCurrentDriver()
    }

    fun onEvent(event: RidesEvent) {
        when (event) {
            RidesEvent.LoadRides -> Unit
            is RidesEvent.SearchChanged -> updateFilters(searchQuery = event.value)
            is RidesEvent.FiltersExpandedChanged -> {
                _uiState.update { it.copy(showFilters = event.expanded) }
            }
            is RidesEvent.AreaSelected -> updateFilters(selectedArea = event.area)
            is RidesEvent.MaxPriceChanged -> updateFilters(maxPrice = event.value)
            is RidesEvent.MinRatingSelected -> updateFilters(selectedMinRating = event.rating)
            RidesEvent.ApplySuggestedDestination -> updateFilters(searchQuery = "Centro")
            RidesEvent.ClearRatingFilter -> updateFilters(selectedMinRating = null)
            is RidesEvent.DriverOriginChanged -> updateDriverForm(origin = event.value)
            is RidesEvent.DriverDestinationChanged -> updateDriverForm(destination = event.value)
            is RidesEvent.DriverDateChanged -> updateDriverForm(date = event.value)
            is RidesEvent.DriverTimeChanged -> updateDriverForm(time = event.value)
            RidesEvent.DriverIncreaseSeats -> {
                updateDriverForm(totalSeats = (_uiState.value.totalSeats + 1).coerceAtMost(6))
            }
            RidesEvent.DriverDecreaseSeats -> {
                updateDriverForm(totalSeats = (_uiState.value.totalSeats - 1).coerceAtLeast(1))
            }
            is RidesEvent.DriverPriceChanged -> updateDriverForm(pricePerSeat = event.value)
            is RidesEvent.DriverCarModelChanged -> updateDriverForm(carModel = event.value)
            is RidesEvent.DriverLicensePlateChanged -> {
                updateDriverForm(licensePlate = event.value.uppercase())
            }
            is RidesEvent.DriverDescriptionChanged -> updateDriverForm(description = event.value)
            is RidesEvent.DriverTabChanged -> {
                _uiState.update { it.copy(driverSelectedTab = event.tab) }
            }
            RidesEvent.PublishRide -> publishRide()
            RidesEvent.DismissTrustNotice -> dismissTrustNotice()
            is RidesEvent.CompleteDriverRide -> completeDriverRide(event.rideId)
            is RidesEvent.CancelDriverRide -> cancelDriverRide(event.rideId)
            is RidesEvent.StartDriverRide -> startDriverRide(event.rideId)
        }
    }

    private fun observeCurrentDriver() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .filterNotNull()
                .collect { user ->
                    currentDriverId = user.id
                    if (observedTrustUserId != user.id) {
                        observedTrustUserId = user.id
                        observeTrustScore(user.id)
                    }
                }
        }
    }

    private fun observeTrustScore(userId: String) {
        viewModelScope.launch {
            driverTrustRepository.observeDriverTrustScore(userId)
                .catch {
                    _uiState.update { state -> state.copy(currentDriverTrustScore = null) }
                }
                .collect { score ->
                    _uiState.update { state ->
                        state.copy(currentDriverTrustScore = score?.reliabilityScore)
                    }
                }
        }
    }

    private fun updateFilters(
        searchQuery: String = _uiState.value.searchQuery,
        selectedArea: String = _uiState.value.selectedArea,
        maxPrice: Float = _uiState.value.maxPrice,
        selectedMinRating: Double? = _uiState.value.selectedMinRating
    ) {
        val filtered = mockRides.filter { ride ->
            val matchesSearch = searchQuery.isBlank() ||
                ride.destination.contains(searchQuery, ignoreCase = true) ||
                ride.origin.contains(searchQuery, ignoreCase = true) ||
                ride.driver.contains(searchQuery, ignoreCase = true)

            val matchesArea = selectedArea == "All Areas" || ride.destinationArea == selectedArea
            val matchesPrice = ride.price <= maxPrice.toInt()
            val matchesRating = selectedMinRating == null || ride.rating >= selectedMinRating

            matchesSearch && matchesArea && matchesPrice && matchesRating
        }

        _uiState.update {
            it.copy(
                searchQuery = searchQuery,
                selectedArea = selectedArea,
                maxPrice = maxPrice,
                selectedMinRating = selectedMinRating,
                filteredRides = filtered
            )
        }
    }

    private fun updateDriverForm(
        origin: String = _uiState.value.origin,
        destination: String = _uiState.value.destination,
        date: String = _uiState.value.date,
        time: String = _uiState.value.time,
        totalSeats: Int = _uiState.value.totalSeats,
        pricePerSeat: String = _uiState.value.pricePerSeat,
        carModel: String = _uiState.value.carModel,
        licensePlate: String = _uiState.value.licensePlate,
        description: String = _uiState.value.description
    ) {
        _uiState.update {
            it.copy(
                origin = origin,
                destination = destination,
                date = date,
                time = time,
                totalSeats = totalSeats,
                pricePerSeat = pricePerSeat,
                carModel = carModel,
                licensePlate = licensePlate,
                description = description
            )
        }
    }

    private fun publishRide() {
        val currentState = _uiState.value
        if (!currentState.canPublishRide) return

        val newRide = DriverRideUiModel(
            id = "driver-${UUID.randomUUID()}",
            origin = currentState.origin,
            destination = currentState.destination,
            date = currentState.date,
            time = currentState.time,
            estimatedArrival = estimateArrival(currentState.time),
            totalSeats = currentState.totalSeats,
            pricePerSeat = currentState.pricePerSeat.toIntOrNull() ?: 0,
            carModel = currentState.carModel,
            licensePlate = currentState.licensePlate,
            status = DriverRideStatus.PENDING,
            passengers = defaultPassengers.take(currentState.totalSeats.coerceAtMost(defaultPassengers.size))
        )

        _uiState.update {
            it.copy(
                driverSelectedTab = DriverRidesTab.MY_RIDES,
                driverRides = (it.driverRides + newRide).sortedBy { ride -> "${ride.date} ${ride.time}" },
                origin = "",
                destination = "",
                date = "",
                time = "",
                totalSeats = 3,
                pricePerSeat = "",
                carModel = "",
                licensePlate = "",
                description = ""
            )
        }
    }

    private fun completeDriverRide(rideId: String) {
        val ride = _uiState.value.driverRides.firstOrNull { it.id == rideId } ?: return
        val driverId = currentDriverId ?: return showTrustError("No signed-in driver is available.")

        viewModelScope.launch {
            _uiState.update { state -> state.copy(actionInProgressRideId = rideId) }
            runCatching {
                val notice = driverTrustRepository.completeRideAndAwaitTrustUpdate(
                    params = ride.toTrustActionParams(driverId)
                )
                notice
            }.onSuccess { notice ->
                _uiState.update { state ->
                    state.copy(
                        driverRides = state.driverRides.map { currentRide ->
                            if (currentRide.id == rideId) {
                                currentRide.copy(
                                    status = DriverRideStatus.COMPLETED,
                                    passengers = currentRide.passengers.mapIndexed { index, passenger ->
                                        passenger.copy(
                                            paymentStatus = if (index == currentRide.passengers.lastIndex) {
                                                PaymentStatusState.PENDING
                                            } else {
                                                PaymentStatusState.PAID
                                            }
                                        )
                                    }
                                )
                            } else {
                                currentRide
                            }
                        },
                        actionInProgressRideId = null,
                        trustNotice = notice,
                        shouldPopAfterTrustNotice = false,
                        ridePendingRemovalId = null
                    )
                }
            }.onFailure { throwable ->
                showTrustError(
                    message = throwable.message ?: "We could not update the ride completion right now.",
                    rideId = rideId
                )
            }
        }
    }

    private fun startDriverRide(rideId: String) {
        val ride = _uiState.value.driverRides.firstOrNull { it.id == rideId } ?: return
        val driverId = currentDriverId ?: return showTrustError("No signed-in driver is available.")

        viewModelScope.launch {
            _uiState.update { state -> state.copy(actionInProgressRideId = rideId) }
            runCatching {
                driverTrustRepository.startRide(ride.toTrustActionParams(driverId))
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        driverRides = state.driverRides.map { currentRide ->
                            if (currentRide.id == rideId) {
                                currentRide.copy(status = DriverRideStatus.ACTIVE)
                            } else {
                                currentRide
                            }
                        },
                        actionInProgressRideId = null
                    )
                }
            }.onFailure { throwable ->
                showTrustError(
                    message = throwable.message ?: "We could not start this ride right now.",
                    rideId = rideId
                )
            }
        }
    }

    private fun cancelDriverRide(rideId: String) {
        val ride = _uiState.value.driverRides.firstOrNull { it.id == rideId } ?: return
        val driverId = currentDriverId ?: return showTrustError("No signed-in driver is available.")

        viewModelScope.launch {
            _uiState.update { state -> state.copy(actionInProgressRideId = rideId) }
            runCatching {
                driverTrustRepository.cancelRideAndAwaitTrustUpdate(
                    params = ride.toTrustActionParams(driverId)
                )
            }.onSuccess { notice ->
                _uiState.update { state ->
                    state.copy(
                        actionInProgressRideId = null,
                        trustNotice = notice,
                        shouldPopAfterTrustNotice = true,
                        ridePendingRemovalId = rideId
                    )
                }
            }.onFailure { throwable ->
                showTrustError(
                    message = throwable.message ?: "We could not cancel this ride right now.",
                    rideId = rideId
                )
            }
        }
    }

    private fun dismissTrustNotice() {
        _uiState.update { state ->
            state.copy(
                driverRides = if (state.ridePendingRemovalId == null) {
                    state.driverRides
                } else {
                    state.driverRides.filterNot { it.id == state.ridePendingRemovalId }
                },
                trustNotice = null,
                shouldPopAfterTrustNotice = false,
                ridePendingRemovalId = null
            )
        }
    }

    private fun showTrustError(message: String, rideId: String? = null) {
        _uiState.update { state ->
            state.copy(
                actionInProgressRideId = if (rideId == null) state.actionInProgressRideId else null,
                trustNotice = TrustScoreNotice(
                    title = "Trust score unavailable",
                    message = message,
                    type = TrustScoreNoticeType.ERROR
                ),
                shouldPopAfterTrustNotice = false,
                ridePendingRemovalId = null
            )
        }
    }

    private fun estimateArrival(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time

        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1].toIntOrNull() ?: return time
        val totalMinutes = hour * 60 + minute + 30
        val arrivalHour = (totalMinutes / 60) % 24
        val arrivalMinute = totalMinutes % 60
        return String.format("%02d:%02d", arrivalHour, arrivalMinute)
    }

    private companion object {
        val defaultPassengers = listOf(
            DriverPassengerUiModel(
                id = "passenger-1",
                name = "Ana Garcia",
                rating = 4.9,
                seat = 1,
                status = "confirmed",
                paymentStatus = PaymentStatusState.PENDING
            ),
            DriverPassengerUiModel(
                id = "passenger-2",
                name = "Pedro Lopez",
                rating = 4.7,
                seat = 2,
                status = "confirmed",
                paymentStatus = PaymentStatusState.PENDING
            ),
            DriverPassengerUiModel(
                id = "passenger-3",
                name = "Maria Diaz",
                rating = 5.0,
                seat = 3,
                status = "confirmed",
                paymentStatus = PaymentStatusState.PENDING
            )
        )
    }
}

sealed interface RidesEvent {
    data object LoadRides : RidesEvent
    data object ApplySuggestedDestination : RidesEvent
    data object ClearRatingFilter : RidesEvent
    data object DriverIncreaseSeats : RidesEvent
    data object DriverDecreaseSeats : RidesEvent
    data object PublishRide : RidesEvent
    data object DismissTrustNotice : RidesEvent
    data class SearchChanged(val value: String) : RidesEvent
    data class FiltersExpandedChanged(val expanded: Boolean) : RidesEvent
    data class AreaSelected(val area: String) : RidesEvent
    data class MaxPriceChanged(val value: Float) : RidesEvent
    data class MinRatingSelected(val rating: Double) : RidesEvent
    data class DriverOriginChanged(val value: String) : RidesEvent
    data class DriverDestinationChanged(val value: String) : RidesEvent
    data class DriverDateChanged(val value: String) : RidesEvent
    data class DriverTimeChanged(val value: String) : RidesEvent
    data class DriverPriceChanged(val value: String) : RidesEvent
    data class DriverCarModelChanged(val value: String) : RidesEvent
    data class DriverLicensePlateChanged(val value: String) : RidesEvent
    data class DriverDescriptionChanged(val value: String) : RidesEvent
    data class DriverTabChanged(val tab: DriverRidesTab) : RidesEvent
    data class CompleteDriverRide(val rideId: String) : RidesEvent
    data class CancelDriverRide(val rideId: String) : RidesEvent
    data class StartDriverRide(val rideId: String) : RidesEvent
}

data class RidesUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFilters: Boolean = false,
    val selectedArea: String = "All Areas",
    val maxPrice: Float = 5000f,
    val selectedMinRating: Double? = null,
    val availableAreas: List<String> = listOf("All Areas", "Chapinero", "Usaquen", "Suba", "Kennedy"),
    val availableRatings: List<Double> = listOf(4.0, 4.5, 4.7, 4.9),
    val allRides: List<RideCardUiModel> = emptyList(),
    val filteredRides: List<RideCardUiModel> = emptyList(),
    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val totalSeats: Int = 3,
    val pricePerSeat: String = "",
    val carModel: String = "",
    val licensePlate: String = "",
    val description: String = "",
    val driverSelectedTab: DriverRidesTab = DriverRidesTab.CREATE_RIDE,
    val driverRides: List<DriverRideUiModel> = emptyList(),
    val currentDriverTrustScore: Int? = null,
    val actionInProgressRideId: String? = null,
    val trustNotice: TrustScoreNotice? = null,
    val shouldPopAfterTrustNotice: Boolean = false,
    val ridePendingRemovalId: String? = null
) {
    val estimatedEarnings: Int
        get() = (pricePerSeat.toIntOrNull() ?: 0) * totalSeats

    val canPublishRide: Boolean
        get() = origin.isNotBlank() &&
            destination.isNotBlank() &&
            date.isNotBlank() &&
            time.isNotBlank() &&
            pricePerSeat.isNotBlank() &&
            carModel.isNotBlank() &&
            licensePlate.isNotBlank()
}

enum class DriverRidesTab {
    CREATE_RIDE,
    MY_RIDES
}

data class DriverRideUiModel(
    val id: String,
    val origin: String,
    val destination: String,
    val date: String,
    val time: String,
    val estimatedArrival: String,
    val totalSeats: Int,
    val pricePerSeat: Int,
    val carModel: String,
    val licensePlate: String,
    val status: DriverRideStatus,
    val passengers: List<DriverPassengerUiModel>
) {
    val formattedSchedule: String
        get() = "$date • $time"

    val totalEarnings: Int
        get() = passengers.size * pricePerSeat

    fun toTrustActionParams(driverId: String): DriverRideTrustActionParams {
        val scheduledStartAtMillis = runCatching {
            val (year, month, day) = date.split("-").map { it.toInt() }
            val (hour, minute) = time.split(":").map { it.toInt() }
            java.util.Calendar.getInstance().apply {
                set(year, month - 1, day, hour, minute, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.getOrElse { System.currentTimeMillis() }

        return DriverRideTrustActionParams(
            rideId = id,
            driverId = driverId,
            scheduledStartAtMillis = scheduledStartAtMillis
        )
    }
}

enum class DriverRideStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

enum class PaymentStatusState {
    PAID,
    PENDING
}

data class DriverPassengerUiModel(
    val id: String,
    val name: String,
    val rating: Double,
    val seat: Int,
    val status: String,
    val paymentStatus: PaymentStatusState
)

data class RideCardUiModel(
    val id: String,
    val driver: String,
    val rating: Double,
    val ridesCount: Int,
    val reliabilityScore: Int,
    val origin: String,
    val destination: String,
    val destinationArea: String,
    val departureTime: String,
    val estimatedDuration: String,
    val price: Int,
    val availableSeats: Int,
    val totalSeats: Int,
    val isHabitRide: Boolean,
    val punctualityRate: Int
) {
    val initials: String
        get() = driver.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    val compactPrice: String
        get() = "$" + String.format("%.1fk", price / 1000f)
}
