package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheels.app.features.rides.domain.usecase.GetAvailableRidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RidesViewModel @Inject constructor(
    private val getAvailableRidesUseCase: GetAvailableRidesUseCase
) : ViewModel() {

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

    private val _uiState = MutableStateFlow(
        RidesUiState(
            allRides = mockRides,
            filteredRides = mockRides
        )
    )
    val uiState: StateFlow<RidesUiState> = _uiState.asStateFlow()

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
}

sealed interface RidesEvent {
    data object LoadRides : RidesEvent
    data object ApplySuggestedDestination : RidesEvent
    data object ClearRatingFilter : RidesEvent
    data class SearchChanged(val value: String) : RidesEvent
    data class FiltersExpandedChanged(val expanded: Boolean) : RidesEvent
    data class AreaSelected(val area: String) : RidesEvent
    data class MaxPriceChanged(val value: Float) : RidesEvent
    data class MinRatingSelected(val rating: Double) : RidesEvent
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
    val filteredRides: List<RideCardUiModel> = emptyList()
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
