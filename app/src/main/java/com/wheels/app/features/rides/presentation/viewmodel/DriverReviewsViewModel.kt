package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.createdAtLabel
import com.wheels.app.features.reviews.domain.model.calculateDriverReviewSummary
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DriverReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: RideReviewRepository
) : ViewModel() {

    private val driverId: String = savedStateHandle.get<String>("driverId").orEmpty()
    private val driverName: String = savedStateHandle.get<String>("driverName").orEmpty()

    private val _uiState = MutableStateFlow(
        DriverReviewsUiState(
            driverId = driverId,
            driverName = driverName.ifBlank { "Driver" }
        )
    )
    val uiState: StateFlow<DriverReviewsUiState> = _uiState.asStateFlow()

    init {
        observeReviews()
    }

    private fun observeReviews() {
        viewModelScope.launch {
            reviewRepository.observeDriverReviews(driverId)
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reviews = emptyList(),
                            summary = DriverReviewSummary(
                                driverId = driverId,
                                reviewCount = 0,
                                ratedReviewCount = 0,
                                averageRating = 0.0,
                                starBreakdown = (5 downTo 1).associateWith { 0 }
                            )
                        )
                    }
                }
                .collect { reviews ->
                    val summary = calculateDriverReviewSummary(reviews)[driverId]
                        ?: DriverReviewSummary(
                            driverId = driverId,
                            reviewCount = reviews.size,
                            ratedReviewCount = reviews.count { it.hasRating },
                            averageRating = 0.0,
                            starBreakdown = (5 downTo 1).associateWith { 0 }
                        )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reviews = reviews,
                            summary = summary
                        )
                    }
                }
        }
    }
}

data class DriverReviewsUiState(
    val driverId: String = "",
    val driverName: String = "Driver",
    val isLoading: Boolean = true,
    val reviews: List<RideReview> = emptyList(),
    val summary: DriverReviewSummary = DriverReviewSummary(
        driverId = "",
        reviewCount = 0,
        ratedReviewCount = 0,
        averageRating = 0.0,
        starBreakdown = (5 downTo 1).associateWith { 0 }
    )
)
