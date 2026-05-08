package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.reviews.domain.model.DriverReviewsFeed
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.createdAtLabel
import com.wheels.app.features.reviews.domain.model.calculateDriverReviewSummary
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import com.wheels.app.features.reviews.data.local.DriverReviewsLocalCache
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
    private val reviewRepository: RideReviewRepository,
    private val reviewsLocalCache: DriverReviewsLocalCache
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
        preloadCachedReviews()
        observeReviews()
    }

    private fun preloadCachedReviews() {
        val cached = reviewsLocalCache.get(driverId) ?: return
        val summary = calculateSummary(cached.reviews)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = true,
            isShowingCachedContent = true,
            reviews = cached.reviews,
            summary = summary,
            errorMessage = null
        )
    }

    private fun observeReviews() {
        viewModelScope.launch {
            reviewRepository.observeDriverReviews(driverId)
                .catch { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Reviews are unavailable right now."
                        )
                    }
                }
                .collect { feed ->
                    when (feed) {
                        is DriverReviewsFeed.Cached -> {
                            val summary = calculateSummary(feed.reviews)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = true,
                                    isShowingCachedContent = true,
                                    reviews = feed.reviews,
                                    summary = summary,
                                    errorMessage = null
                                )
                            }
                        }

                        is DriverReviewsFeed.Fresh -> {
                            val summary = calculateSummary(feed.reviews)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    isShowingCachedContent = false,
                                    reviews = feed.reviews,
                                    summary = summary,
                                    errorMessage = null
                                )
                            }
                        }

                        is DriverReviewsFeed.RefreshError -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = feed.message
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun calculateSummary(reviews: List<RideReview>): DriverReviewSummary {
        return calculateDriverReviewSummary(reviews)[driverId]
            ?: DriverReviewSummary(
                driverId = driverId,
                reviewCount = reviews.size,
                ratedReviewCount = reviews.count { it.hasRating },
                averageRating = 0.0,
                starBreakdown = (5 downTo 1).associateWith { 0 }
            )
    }
}

data class DriverReviewsUiState(
    val driverId: String = "",
    val driverName: String = "Driver",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isShowingCachedContent: Boolean = false,
    val errorMessage: String? = null,
    val reviews: List<RideReview> = emptyList(),
    val summary: DriverReviewSummary = DriverReviewSummary(
        driverId = "",
        reviewCount = 0,
        ratedReviewCount = 0,
        averageRating = 0.0,
        starBreakdown = (5 downTo 1).associateWith { 0 }
    )
)
