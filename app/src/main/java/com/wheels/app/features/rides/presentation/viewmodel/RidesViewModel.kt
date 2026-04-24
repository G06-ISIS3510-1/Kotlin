package com.wheels.app.features.rides.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.location.domain.model.CurrentCoordinates
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.trust.domain.model.TrustScoreNotice
import com.wheels.app.core.trust.domain.model.TrustScoreNoticeType
import com.wheels.app.core.trust.domain.repository.DriverRideTrustActionParams
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.rides.domain.repository.CancellationBehaviorRepository
import com.wheels.app.features.rides.domain.usecase.ShouldShowBehavioralNudgeUseCase
import com.wheels.app.features.rides.domain.model.BehavioralNudge
import com.wheels.app.features.rides.domain.model.CancellationBehaviorMetrics
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.repository.RideRepository
import com.wheels.app.features.rides.presentation.mock.OriginAutocompleteMocks
import com.wheels.app.features.rides.presentation.model.LocationSuggestion
import com.wheels.app.features.rides.presentation.model.LocationSuggestionType
import com.wheels.app.features.rides.presentation.model.RideLocationField
import com.wheels.app.features.rides.domain.usecase.GetAvailableRidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

private const val DEFAULT_MAX_PRICE_FILTER = 20000f
private const val ORIGIN_MAX_LENGTH = 120
private const val DESTINATION_MAX_LENGTH = 120
private const val PRICE_MAX_LENGTH = 6
private const val CAR_MODEL_MAX_LENGTH = 60
private const val LICENSE_PLATE_MAX_LENGTH = 10
private const val DESCRIPTION_MAX_LENGTH = 180
private const val NEARBY_DISTANCE_KM_THRESHOLD = 5.0

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val getAvailableRidesUseCase: GetAvailableRidesUseCase,
    private val rideRepository: RideRepository,
    private val authRepository: AuthRepository,
    private val driverTrustRepository: DriverTrustRepository,
    private val cancellationBehaviorRepository: CancellationBehaviorRepository,
    private val shouldShowBehavioralNudgeUseCase: ShouldShowBehavioralNudgeUseCase,
    private val currentLocationProvider: CurrentLocationProvider,
    roleManager: RoleManager
) : ViewModel() {

    private var currentDriverId: String? = null
    private var currentDriverName: String = ""
    private var currentDriverEmail: String = ""
    private var currentDriverRating: Int = 5
    private var observedTrustUserId: String? = null
    private var observedCancellationMetricsUserId: String? = null
    private var observedDriverRidesUserId: String? = null
    private val rideOriginCoordinatesCache = mutableMapOf<String, CurrentCoordinates>()
    private var rideDistanceKmById: Map<String, Double> = emptyMap()

    private fun newBackendRideId(prefix: String = "ride"): String = "$prefix-${UUID.randomUUID()}"

    private val seedDriverRides = listOf(
        DriverRideUiModel(
            id = "driver-1",
            backendRideId = newBackendRideId("mock-driver-1"),
            origin = "Campus Uniandes - Main Gate",
            destination = "Centro Comercial Andino",
            date = "2026-04-15",
            time = "17:00",
            estimatedArrival = "17:30",
            totalSeats = 3,
            pricePerSeat = 3500,
            carModel = "Toyota Corolla 2020",
            licensePlate = "ABC-123",
            status = DriverRideStatus.ACTIVE,
            passengers = defaultPassengers
        ),
        DriverRideUiModel(
            id = "driver-2",
            backendRideId = newBackendRideId("mock-driver-2"),
            origin = "Campus Uniandes - ML Building",
            destination = "Usaquen",
            date = "2026-04-15",
            time = "17:00",
            estimatedArrival = "17:35",
            totalSeats = 2,
            pricePerSeat = 4000,
            carModel = "Mazda 3 2021",
            licensePlate = "XYZ-456",
            status = DriverRideStatus.PENDING,
            passengers = defaultPassengers.take(2).map { it.copy(paymentStatus = PaymentStatusState.PENDING) }
        ),
        DriverRideUiModel(
            id = "driver-3",
            backendRideId = newBackendRideId("mock-driver-3"),
            origin = "Campus Uniandes - Entrance Gate",
            destination = "Suba Centro",
            date = "2026-04-15",
            time = "17:00",
            estimatedArrival = "17:40",
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
            allRides = emptyList(),
            filteredRides = emptyList(),
            driverRides = emptyList()
        )
    )
    val uiState: StateFlow<RidesUiState> = _uiState.asStateFlow()
    val activeRole: StateFlow<UserRole> = roleManager.activeRole

    init {
        observeAvailableRides()
        observeCurrentDriver()
    }

    fun onEvent(event: RidesEvent) {
        when (event) {
            RidesEvent.LoadRides -> Unit
            is RidesEvent.SearchChanged -> updateFilters(searchQuery = event.value)
            is RidesEvent.FiltersExpandedChanged -> {
                _uiState.update { it.copy(showFilters = event.expanded) }
            }
            is RidesEvent.ApplyNearbyRides -> applyNearbyRides(event.locationNameHint)
            RidesEvent.ClearNearbyRides -> clearNearbyRides()
            is RidesEvent.AreaSelected -> updateFilters(selectedArea = event.area)
            is RidesEvent.MaxPriceChanged -> updateFilters(maxPrice = event.value)
            is RidesEvent.MinRatingSelected -> updateFilters(selectedMinRating = event.rating)
            RidesEvent.ApplySuggestedDestination -> applySmartSuggestion()
            RidesEvent.ClearRatingFilter -> updateFilters(selectedMinRating = null)
            RidesEvent.ClearPassengerFilters -> clearPassengerFilters()
            is RidesEvent.DriverLocationQueryChanged -> updateLocationQuery(event.field, event.value)
            is RidesEvent.DriverLocationFieldFocused -> showLocationSuggestions(event.field)
            is RidesEvent.DriverLocationSuggestionSelected -> selectLocationSuggestion(
                field = event.field,
                suggestion = event.suggestion
            )
            is RidesEvent.DriverUseCurrentLocation -> useCurrentLocation(event.field)
            is RidesEvent.DriverDateChanged -> updateSchedule(date = event.value)
            is RidesEvent.DriverTimeChanged -> updateSchedule(time = event.value)
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
            is RidesEvent.DeleteDriverRide -> deleteDriverRide(event.rideId)
        }
    }

    private fun observeCurrentDriver() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .collect { user ->
                    if (user == null) {
                        currentDriverId = null
                        currentDriverName = ""
                        currentDriverEmail = ""
                        currentDriverRating = 5
                        observedTrustUserId = null
                        observedCancellationMetricsUserId = null
                        observedDriverRidesUserId = null
                        _uiState.update { state ->
                            state.copy(
                                currentDriverTrustScore = null,
                                cancellationBehaviorMetrics = null,
                                behavioralNudge = null,
                                driverRides = emptyList(),
                                isLoadingDriverRides = false
                            )
                        }
                    } else {
                        currentDriverId = user.id
                        currentDriverName = user.fullName.ifBlank {
                            user.email.substringBefore("@").replace('.', ' ')
                        }
                        currentDriverEmail = user.email
                        currentDriverRating = if (user.rating > 0.0) {
                            user.rating.roundToInt().coerceIn(1, 5)
                        } else {
                            5
                        }
                        if (observedTrustUserId != user.id) {
                            observedTrustUserId = user.id
                            observeTrustScore(user.id)
                        }
                        if (observedCancellationMetricsUserId != user.id) {
                            observedCancellationMetricsUserId = user.id
                            observeCancellationBehaviorMetrics(user.id)
                        }
                        if (observedDriverRidesUserId != user.id) {
                            observedDriverRidesUserId = user.id
                            observeDriverRides(user.id)
                        }
                    }
                }
        }
    }

    private fun observeAvailableRides() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }
            getAvailableRidesUseCase()
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allRides = emptyList(),
                            filteredRides = emptyList(),
                            availableAreas = listOf("All Areas")
                        )
                    }
                }
                .collect { rides ->
                    val rideCards = rides.toRideCards()
                    val availableAreas = buildAvailableAreas(rideCards)
                    val selectedArea = _uiState.value.selectedArea
                    val nearbyLocationName = _uiState.value.nearbyRides.locationName
                    val nearbyModeRequested = _uiState.value.nearbyRides.isActive ||
                        _uiState.value.nearbyRides.isLoading
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allRides = rideCards,
                            availableAreas = availableAreas,
                            selectedArea = if (selectedArea in availableAreas) {
                                selectedArea
                            } else {
                                "All Areas"
                            },
                            smartSuggestion = buildSmartSuggestion(rideCards)
                        )
                    }
                    if (nearbyModeRequested) {
                        applyNearbyRides(nearbyLocationName)
                    } else {
                        applyPassengerFilters()
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

    private fun observeCancellationBehaviorMetrics(userId: String) {
        viewModelScope.launch {
            cancellationBehaviorRepository.observeCancellationBehaviorMetrics(userId)
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            cancellationBehaviorMetrics = null,
                            behavioralNudge = null
                        )
                    }
                }
                .collect { metrics ->
                    _uiState.update { state ->
                        state.copy(
                            cancellationBehaviorMetrics = metrics,
                            behavioralNudge = shouldShowBehavioralNudgeUseCase(metrics)
                        )
                    }
                }
        }
    }

    private fun observeDriverRides(userId: String) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoadingDriverRides = true) }
            rideRepository.observeDriverRides(userId)
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoadingDriverRides = false,
                            driverRides = emptyList()
                        )
                    }
                }
                .collect { rides ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingDriverRides = false,
                            driverRides = rides.map { it.toUiModel() }
                        )
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
        _uiState.update {
            it.copy(
                searchQuery = searchQuery,
                selectedArea = selectedArea,
                maxPrice = maxPrice,
                selectedMinRating = selectedMinRating
            )
        }
        applyPassengerFilters()
    }

    private fun applyPassengerFilters() {
        val currentState = _uiState.value
        val filtered = currentState.allRides
            .filter { ride ->
                val matchesSearch = currentState.searchQuery.isBlank() ||
                    ride.destination.contains(currentState.searchQuery, ignoreCase = true) ||
                    ride.origin.contains(currentState.searchQuery, ignoreCase = true) ||
                    ride.driver.contains(currentState.searchQuery, ignoreCase = true)

                val matchesArea = currentState.selectedArea == "All Areas" ||
                    ride.destinationArea.equals(currentState.selectedArea, ignoreCase = true)
                val matchesPrice = ride.price <= currentState.maxPrice.toInt()
                val matchesRating = currentState.selectedMinRating == null ||
                    ride.rating >= currentState.selectedMinRating

                matchesSearch && matchesArea && matchesPrice && matchesRating
            }
            .map { ride ->
                ride.copy(distanceFromCurrentLocationKm = rideDistanceKmById[ride.id])
            }
            .sortedWith(
                if (currentState.nearbyRides.isActive) {
                    compareBy<RideCardUiModel> { it.distanceFromCurrentLocationKm ?: Double.MAX_VALUE }
                        .thenByDescending { it.reliabilityScore }
                        .thenBy { it.departureTimestamp }
                } else {
                    compareByDescending<RideCardUiModel> { it.reliabilityScore }
                        .thenBy { it.departureTimestamp }
                }
            )
            .mapIndexed { index, ride ->
                ride.copy(
                    isRecommendedByTrustScore = !currentState.nearbyRides.isActive &&
                        index < TRUST_RECOMMENDATION_COUNT
                )
            }

        _uiState.update {
            it.copy(filteredRides = filtered)
        }
    }

    private fun buildAvailableAreas(rides: List<RideCardUiModel>): List<String> {
        val dynamicAreas = rides
            .map { it.destinationArea.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        return listOf("All Areas") + dynamicAreas
    }

    private fun applySmartSuggestion() {
        val suggestion = _uiState.value.smartSuggestion ?: return
        updateFilters(
            searchQuery = suggestion.query,
            selectedArea = "All Areas"
        )
    }

    private fun clearPassengerFilters() {
        rideDistanceKmById = emptyMap()
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedArea = "All Areas",
                maxPrice = DEFAULT_MAX_PRICE_FILTER,
                selectedMinRating = null,
                nearbyRides = NearbyRidesUiState()
            )
        }
        applyPassengerFilters()
    }

    private fun clearNearbyRides() {
        rideDistanceKmById = emptyMap()
        _uiState.update { state ->
            state.copy(nearbyRides = NearbyRidesUiState())
        }
        applyPassengerFilters()
    }

    private fun applyNearbyRides(locationNameHint: String?) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    searchQuery = "",
                    selectedArea = "All Areas",
                    maxPrice = DEFAULT_MAX_PRICE_FILTER,
                    selectedMinRating = null,
                    nearbyRides = state.nearbyRides.copy(
                        isLoading = true,
                        isActive = false,
                        locationName = locationNameHint ?: state.nearbyRides.locationName,
                        errorMessage = null
                    )
                )
            }
            applyPassengerFilters()

            if (_uiState.value.allRides.isEmpty() && _uiState.value.isLoading) {
                return@launch
            }

            runCatching {
                val currentCoordinates = currentLocationProvider.getCurrentCoordinates()
                resolveRideDistances(currentCoordinates)
            }.onSuccess { distancesByRideId ->
                rideDistanceKmById = distancesByRideId
                val nearbyRideCount = distancesByRideId.values.count { it <= NEARBY_DISTANCE_KM_THRESHOLD }

                _uiState.update { state ->
                    state.copy(
                        nearbyRides = NearbyRidesUiState(
                            isActive = distancesByRideId.isNotEmpty(),
                            isLoading = false,
                            locationName = locationNameHint ?: state.nearbyRides.locationName,
                            nearbyRideCount = nearbyRideCount,
                            errorMessage = if (distancesByRideId.isEmpty()) {
                                "We could not resolve ride origins near your current area."
                            } else {
                                null
                            }
                        )
                    )
                }
                applyPassengerFilters()
            }.onFailure { throwable ->
                rideDistanceKmById = emptyMap()
                _uiState.update { state ->
                    state.copy(
                        nearbyRides = NearbyRidesUiState(
                            isActive = false,
                            isLoading = false,
                            locationName = locationNameHint ?: state.nearbyRides.locationName,
                            nearbyRideCount = 0,
                            errorMessage = throwable.message ?: "We could not load nearby rides right now."
                        )
                    )
                }
                applyPassengerFilters()
            }
        }
    }

    private suspend fun resolveRideDistances(currentCoordinates: CurrentCoordinates): Map<String, Double> {
        val rides = _uiState.value.allRides
        return rides.mapNotNull { ride ->
            val distanceKm = resolveRideDistanceKm(
                currentCoordinates = currentCoordinates,
                ride = ride
            ) ?: return@mapNotNull null

            ride.id to distanceKm
        }.toMap()
    }

    private suspend fun resolveRideDistanceKm(
        currentCoordinates: CurrentCoordinates,
        ride: RideCardUiModel
    ): Double? {
        ride.originCoordinates?.let { originCoordinates ->
            return calculateDistanceKm(
                start = currentCoordinates,
                end = originCoordinates.toCurrentCoordinates()
            )
        }

        val normalizedOrigin = ride.origin.trim()
        if (normalizedOrigin.isBlank()) {
            return null
        }

        val originCoordinates = rideOriginCoordinatesCache[normalizedOrigin]
            ?: currentLocationProvider.geocodeAddress(normalizedOrigin)?.also { resolvedCoordinates ->
                rideOriginCoordinatesCache[normalizedOrigin] = resolvedCoordinates
            }
            ?: return null

        return calculateDistanceKm(
            start = currentCoordinates,
            end = originCoordinates
        )
    }

    private suspend fun resolveCoordinatesForPublish(
        typedValue: String,
        selectedSuggestion: LocationSuggestion?
    ): Coordinates? {
        selectedSuggestion?.coordinates?.let { return it }

        val normalizedValue = typedValue.trim()
        if (normalizedValue.isBlank()) {
            return null
        }

        return currentLocationProvider.geocodeAddress(normalizedValue)?.toRideCoordinates()
    }

    private fun calculateDistanceKm(
        start: CurrentCoordinates,
        end: CurrentCoordinates
    ): Double {
        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            result
        )
        return result.firstOrNull()?.div(1000.0) ?: Double.MAX_VALUE
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
                origin = origin.take(ORIGIN_MAX_LENGTH),
                destination = destination.take(DESTINATION_MAX_LENGTH),
                date = date,
                time = time,
                totalSeats = totalSeats,
                pricePerSeat = pricePerSeat.take(PRICE_MAX_LENGTH),
                carModel = carModel.take(CAR_MODEL_MAX_LENGTH),
                licensePlate = licensePlate.take(LICENSE_PLATE_MAX_LENGTH),
                description = description.take(DESCRIPTION_MAX_LENGTH),
                publishRideErrorMessage = null
            )
        }
    }

    private fun updateSchedule(
        date: String = _uiState.value.date,
        time: String = _uiState.value.time
    ) {
        val validationMessage = validateSchedule(date = date, time = time)
        _uiState.update {
            it.copy(
                date = date,
                time = time,
                scheduleValidationMessage = validationMessage,
                publishRideErrorMessage = null
            )
        }
    }

    private fun updateLocationQuery(field: RideLocationField, value: String) {
        val sanitizedValue = when (field) {
            RideLocationField.ORIGIN -> value.take(ORIGIN_MAX_LENGTH)
            RideLocationField.DESTINATION -> value.take(DESTINATION_MAX_LENGTH)
        }
        val filteredSuggestions = filterLocationSuggestions(sanitizedValue)
        _uiState.update { state ->
            when (field) {
                RideLocationField.ORIGIN -> state.copy(
                    origin = sanitizedValue,
                    selectedOrigin = null,
                    originSuggestions = filteredSuggestions,
                    showOriginSuggestions = true,
                    originNoResults = sanitizedValue.isNotBlank() && filteredSuggestions.isEmpty(),
                    originLocationError = null
                )
                RideLocationField.DESTINATION -> state.copy(
                    destination = sanitizedValue,
                    selectedDestination = null,
                    destinationSuggestions = filteredSuggestions,
                    showDestinationSuggestions = true,
                    destinationNoResults = sanitizedValue.isNotBlank() && filteredSuggestions.isEmpty(),
                    destinationLocationError = null
                )
            }
        }
    }

    private fun showLocationSuggestions(field: RideLocationField) {
        val query = when (field) {
            RideLocationField.ORIGIN -> _uiState.value.origin.trim()
            RideLocationField.DESTINATION -> _uiState.value.destination.trim()
        }
        val filteredSuggestions = filterLocationSuggestions(query)

        _uiState.update { state ->
            when (field) {
                RideLocationField.ORIGIN -> state.copy(
                    originSuggestions = filteredSuggestions,
                    showOriginSuggestions = true,
                    originNoResults = query.isNotBlank() && filteredSuggestions.isEmpty()
                )
                RideLocationField.DESTINATION -> state.copy(
                    destinationSuggestions = filteredSuggestions,
                    showDestinationSuggestions = true,
                    destinationNoResults = query.isNotBlank() && filteredSuggestions.isEmpty()
                )
            }
        }
    }

    private fun selectLocationSuggestion(field: RideLocationField, suggestion: LocationSuggestion) {
        _uiState.update { state ->
            when (field) {
                RideLocationField.ORIGIN -> state.copy(
                    origin = suggestion.title,
                    selectedOrigin = suggestion,
                    originSuggestions = emptyList(),
                    showOriginSuggestions = false,
                    originNoResults = false,
                    originLocationError = null
                )
                RideLocationField.DESTINATION -> state.copy(
                    destination = suggestion.title,
                    selectedDestination = suggestion,
                    destinationSuggestions = emptyList(),
                    showDestinationSuggestions = false,
                    destinationNoResults = false,
                    destinationLocationError = null
                )
            }
        }
    }

    private fun useCurrentLocation(field: RideLocationField) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentLocationLoadingField = field,
                    originLocationError = if (field == RideLocationField.ORIGIN) null else state.originLocationError,
                    destinationLocationError = if (field == RideLocationField.DESTINATION) null else state.destinationLocationError
                )
            }

            runCatching {
                val currentLocation = currentLocationProvider.getCurrentLocationLabel()
                val currentCoordinates = currentLocationProvider.getCurrentCoordinates().toRideCoordinates()
                currentLocation to currentCoordinates
            }.onSuccess { (currentLocation, currentCoordinates) ->
                    selectLocationSuggestion(
                        field = field,
                        suggestion = LocationSuggestion(
                            id = "${field.name.lowercase()}-current-location",
                            title = currentLocation.title,
                            subtitle = currentLocation.subtitle,
                            coordinates = currentCoordinates,
                            type = LocationSuggestionType.CURRENT_LOCATION
                        )
                    )
                    _uiState.update { state -> state.copy(currentLocationLoadingField = null) }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        when (field) {
                            RideLocationField.ORIGIN -> state.copy(
                                currentLocationLoadingField = null,
                                originLocationError = throwable.message ?: "We could not get your current location."
                            )
                            RideLocationField.DESTINATION -> state.copy(
                                currentLocationLoadingField = null,
                                destinationLocationError = throwable.message ?: "We could not get your current location."
                            )
                        }
                    }
                }
        }
    }

    private fun publishRide() {
        val currentState = _uiState.value
        if (!currentState.canPublishRide) return
        val driverId = currentDriverId ?: return showTrustError("No signed-in driver is available.")
        val departureAt = buildRideDateTime(
            date = currentState.date,
            time = currentState.time
        ) ?: return showTrustError("We could not parse the selected ride schedule.")
        val estimatedDurationMinutes = 30

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isPublishingRide = true) }
            runCatching {
                val originCoordinates = resolveCoordinatesForPublish(
                    typedValue = currentState.origin,
                    selectedSuggestion = currentState.selectedOrigin
                )
                val destinationCoordinates = resolveCoordinatesForPublish(
                    typedValue = currentState.destination,
                    selectedSuggestion = currentState.selectedDestination
                )

                rideRepository.publishRide(
                    PublishRideRequest(
                        driverId = driverId,
                        driverName = currentDriverName,
                        driverEmail = currentDriverEmail,
                        origin = currentState.origin,
                        originSearch = normalizeSearchValue(currentState.origin),
                        originCoordinates = originCoordinates,
                        destination = currentState.destination,
                        destinationSearch = normalizeSearchValue(currentState.destination),
                        destinationCoordinates = destinationCoordinates,
                        departureAt = departureAt,
                        estimatedDurationMinutes = estimatedDurationMinutes,
                        totalSeats = currentState.totalSeats,
                        pricePerSeat = currentState.pricePerSeat.toIntOrNull() ?: 0,
                        carModel = currentState.carModel,
                        licensePlate = currentState.licensePlate,
                        notes = currentState.description,
                        driverRating = currentDriverRating,
                        onTimeRate = 100,
                        reviewCount = 0,
                        verifiedByUniversity = true
                    )
                )
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isPublishingRide = false,
                        driverSelectedTab = DriverRidesTab.MY_RIDES,
                        origin = "",
                        selectedOrigin = null,
                        originSuggestions = emptyList(),
                        showOriginSuggestions = false,
                        originNoResults = false,
                        originLocationError = null,
                        destination = "",
                        selectedDestination = null,
                        destinationSuggestions = emptyList(),
                        showDestinationSuggestions = false,
                        destinationNoResults = false,
                        destinationLocationError = null,
                        date = "",
                        time = "",
                        totalSeats = 3,
                        pricePerSeat = "",
                        carModel = "",
                        licensePlate = "",
                        description = "",
                        currentLocationLoadingField = null,
                        scheduleValidationMessage = null,
                        publishRideErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isPublishingRide = false,
                        publishRideErrorMessage = throwable.message
                            ?: "We could not publish this ride right now."
                    )
                }
            }
        }
    }

    private fun completeDriverRide(rideId: String) {
        val ride = _uiState.value.driverRides.firstOrNull { it.id == rideId } ?: return
        if (ride.status != DriverRideStatus.ACTIVE) {
            return showTrustError("Only active rides can be completed.")
        }
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
        if (ride.status != DriverRideStatus.PENDING) {
            return showTrustError("Only pending rides can be started.")
        }
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
        if (ride.status != DriverRideStatus.PENDING) {
            return showTrustError("Only pending rides can be canceled from this screen.")
        }
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
                        shouldPopAfterTrustNotice = false,
                        ridePendingRemovalId = null
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
                trustNotice = null,
                shouldPopAfterTrustNotice = false,
                ridePendingRemovalId = null
            )
        }
    }

    private fun deleteDriverRide(rideId: String) {
        val ride = _uiState.value.driverRides.firstOrNull { it.id == rideId } ?: return
        if (ride.status != DriverRideStatus.COMPLETED && ride.status != DriverRideStatus.CANCELLED) {
            return showTrustError("Only completed or cancelled rides can be deleted from My Rides.")
        }

        viewModelScope.launch {
            _uiState.update { state -> state.copy(actionInProgressRideId = rideId) }
            runCatching {
                rideRepository.deleteDriverRide(ride.backendRideId)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        driverRides = state.driverRides.filterNot { it.id == rideId },
                        actionInProgressRideId = null,
                        trustNotice = null,
                        shouldPopAfterTrustNotice = true
                    )
                }
            }.onFailure { throwable ->
                showTrustError(
                    message = throwable.message ?: "We could not delete this ride right now.",
                    rideId = rideId
                )
            }
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

    private fun buildRideDateTime(date: String, time: String): java.time.Instant? {
        val selectedDate = runCatching {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull() ?: return null

        val selectedTime = runCatching {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrNull() ?: return null

        return selectedDate.atTime(selectedTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
    }

    private fun filterLocationSuggestions(query: String): List<LocationSuggestion> {
        if (query.isBlank()) {
            return OriginAutocompleteMocks.locationSuggestions
        }

        return OriginAutocompleteMocks.locationSuggestions.filter { suggestion ->
            suggestion.title.contains(query, ignoreCase = true) ||
                suggestion.subtitle?.contains(query, ignoreCase = true) == true
        }
    }

    private companion object {
        const val TRUST_RECOMMENDATION_COUNT = 3
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
    data class ApplyNearbyRides(val locationNameHint: String?) : RidesEvent
    data object ClearNearbyRides : RidesEvent
    data object ClearRatingFilter : RidesEvent
    data object ClearPassengerFilters : RidesEvent
    data object DriverIncreaseSeats : RidesEvent
    data object DriverDecreaseSeats : RidesEvent
    data object PublishRide : RidesEvent
    data object DismissTrustNotice : RidesEvent
    data class SearchChanged(val value: String) : RidesEvent
    data class FiltersExpandedChanged(val expanded: Boolean) : RidesEvent
    data class AreaSelected(val area: String) : RidesEvent
    data class MaxPriceChanged(val value: Float) : RidesEvent
    data class MinRatingSelected(val rating: Double) : RidesEvent
    data class DriverLocationQueryChanged(val field: RideLocationField, val value: String) : RidesEvent
    data class DriverLocationFieldFocused(val field: RideLocationField) : RidesEvent
    data class DriverLocationSuggestionSelected(
        val field: RideLocationField,
        val suggestion: LocationSuggestion
    ) : RidesEvent
    data class DriverUseCurrentLocation(val field: RideLocationField) : RidesEvent
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
    data class DeleteDriverRide(val rideId: String) : RidesEvent
}

data class RidesUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFilters: Boolean = false,
    val selectedArea: String = "All Areas",
    val maxPrice: Float = DEFAULT_MAX_PRICE_FILTER,
    val selectedMinRating: Double? = null,
    val availableAreas: List<String> = listOf("All Areas", "Chapinero", "Usaquen", "Suba", "Kennedy"),
    val availableRatings: List<Double> = listOf(4.0, 4.5, 4.7, 4.9),
    val allRides: List<RideCardUiModel> = emptyList(),
    val filteredRides: List<RideCardUiModel> = emptyList(),
    val nearbyRides: NearbyRidesUiState = NearbyRidesUiState(),
    val smartSuggestion: PassengerSmartSuggestion? = null,
    val origin: String = "",
    val selectedOrigin: LocationSuggestion? = null,
    val originSuggestions: List<LocationSuggestion> = emptyList(),
    val currentLocationSuggestion: LocationSuggestion = OriginAutocompleteMocks.currentLocationSuggestion,
    val showOriginSuggestions: Boolean = false,
    val originNoResults: Boolean = false,
    val originLocationError: String? = null,
    val destination: String = "",
    val selectedDestination: LocationSuggestion? = null,
    val destinationSuggestions: List<LocationSuggestion> = emptyList(),
    val showDestinationSuggestions: Boolean = false,
    val destinationNoResults: Boolean = false,
    val destinationLocationError: String? = null,
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
    val ridePendingRemovalId: String? = null,
    val currentLocationLoadingField: RideLocationField? = null,
    val cancellationBehaviorMetrics: CancellationBehaviorMetrics? = null,
    val behavioralNudge: BehavioralNudge? = null,
    val scheduleValidationMessage: String? = null,
    val isPublishingRide: Boolean = false,
    val publishRideErrorMessage: String? = null,
    val isLoadingDriverRides: Boolean = false
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
            licensePlate.isNotBlank() &&
            scheduleValidationMessage == null &&
            !isPublishingRide
}

data class NearbyRidesUiState(
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val locationName: String? = null,
    val nearbyRideCount: Int = 0,
    val errorMessage: String? = null
)

enum class DriverRidesTab {
    CREATE_RIDE,
    MY_RIDES
}

data class DriverRideUiModel(
    val id: String,
    val backendRideId: String,
    val origin: String,
    val destination: String,
    val destinationCoordinates: Coordinates? = null,
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
            rideId = backendRideId,
            driverId = driverId,
            scheduledStartAtMillis = scheduledStartAtMillis
        )
    }
}

private fun validateSchedule(date: String, time: String): String? {
    if (date.isBlank() || time.isBlank()) return null

    val selectedDate = runCatching {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull() ?: return null

    val selectedTime = runCatching {
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null

    val now = LocalDateTime.now()
    if (selectedDate != now.toLocalDate()) return null

    return if (selectedTime.isBefore(now.toLocalTime().withSecond(0).withNano(0))) {
        "If the ride is today, choose a departure time later than the current time."
    } else {
        null
    }
}

private fun DriverRideRecord.toUiModel(): DriverRideUiModel {
    val dateTime = departureAt.atZone(java.time.ZoneId.systemDefault())
    val arrivalDateTime = departureAt
        .plusSeconds(estimatedDurationMinutes * 60L)
        .atZone(java.time.ZoneId.systemDefault())

    return DriverRideUiModel(
        id = id,
        backendRideId = id,
        origin = origin,
        destination = destination,
        destinationCoordinates = destinationCoordinates,
        date = dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
        time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        estimatedArrival = arrivalDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        status = status.toDriverRideStatus(),
        passengers = defaultDriverPassengers()
            .take(totalSeats.coerceAtMost(defaultDriverPassengers().size))
    )
}

private fun normalizeSearchValue(value: String): String {
    return value.trim().lowercase()
}

private fun String.toDriverRideStatus(): DriverRideStatus {
    return when (this) {
        "completed" -> DriverRideStatus.COMPLETED
        "canceled" -> DriverRideStatus.CANCELLED
        "in_progress" -> DriverRideStatus.ACTIVE
        "published" -> DriverRideStatus.PENDING
        else -> DriverRideStatus.PENDING
    }
}

private fun defaultDriverPassengers(): List<DriverPassengerUiModel> {
    return listOf(
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

enum class DriverRideStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED
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
    val originCoordinates: Coordinates? = null,
    val destination: String,
    val destinationArea: String,
    val departureTime: String,
    val estimatedDuration: String,
    val departureTimestamp: Long,
    val price: Int,
    val availableSeats: Int,
    val totalSeats: Int,
    val isHabitRide: Boolean,
    val punctualityRate: Int,
    val distanceFromCurrentLocationKm: Double? = null,
    val isRecommendedByTrustScore: Boolean = false
) {
    val initials: String
        get() = driver.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    val compactPrice: String
        get() = "$" + String.format("%.1fk", price / 1000f)

    val distanceFromCurrentLocationLabel: String?
        get() = distanceFromCurrentLocationKm?.let { distanceKm ->
            if (distanceKm < 1) {
                "${(distanceKm * 1000).roundToInt()} m away"
            } else {
                String.format(Locale.US, "%.1f km away", distanceKm)
            }
        }
}

data class PassengerSmartSuggestion(
    val query: String,
    val message: String
)

private fun buildSmartSuggestion(rides: List<RideCardUiModel>): PassengerSmartSuggestion? {
    val suggestedRide = rides.maxWithOrNull(
        compareBy<RideCardUiModel> { it.reliabilityScore }
            .thenByDescending { it.availableSeats }
    ) ?: return null

    val suggestedQuery = suggestedRide.destinationArea.ifBlank { suggestedRide.destination }
    return PassengerSmartSuggestion(
        query = suggestedQuery,
        message = "Highest-trust rides available right now are heading to $suggestedQuery."
    )
}

private fun List<Ride>.toRideCards(): List<RideCardUiModel> {
    return map { ride ->
        val departure = ride.departureTime.atZone(java.time.ZoneId.systemDefault())
        RideCardUiModel(
            id = ride.id,
            driver = ride.driverName.ifBlank { "Uniandes driver" },
            rating = ride.driverRating,
            ridesCount = ride.reviewCount,
            reliabilityScore = ride.reliabilityScore,
            origin = ride.origin,
            originCoordinates = ride.originCoordinates,
            destination = ride.destination,
            destinationArea = ride.destinationArea.ifBlank { ride.destination },
            departureTime = departure.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            estimatedDuration = "${ride.estimatedDurationMinutes} min",
            departureTimestamp = ride.departureTime.toEpochMilli(),
            price = ride.pricePerSeat.roundToInt(),
            availableSeats = ride.availableSeats,
            totalSeats = ride.totalSeats,
            isHabitRide = ride.isHabitRide,
            punctualityRate = ride.punctualityRate
        )
    }
}

private fun CurrentCoordinates.toRideCoordinates(): Coordinates {
    return Coordinates(
        lat = latitude,
        lng = longitude
    )
}

private fun Coordinates.toCurrentCoordinates(): CurrentCoordinates {
    return CurrentCoordinates(
        latitude = lat,
        longitude = lng
    )
}
