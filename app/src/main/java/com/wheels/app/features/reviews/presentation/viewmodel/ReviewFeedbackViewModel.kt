package com.wheels.app.features.reviews.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewFeedbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val reviewRepository: RideReviewRepository
) : ViewModel() {

    private val rideId: String = savedStateHandle.get<String>("rideId").orEmpty()
    private val driverId: String = savedStateHandle.get<String>("driverId").orEmpty()
    private val driverName: String = savedStateHandle.get<String>("driverName").orEmpty()

    private val _uiState = MutableStateFlow(
        ReviewFeedbackUiState(
            rideId = rideId,
            driverId = driverId,
            driverName = driverName.ifBlank { "Driver" }
        )
    )
    val uiState: StateFlow<ReviewFeedbackUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    fun onEvent(event: ReviewFeedbackEvent) {
        when (event) {
            is ReviewFeedbackEvent.StarSelected -> {
                _uiState.update { current ->
                    current.copy(
                        selectedStars = if (current.selectedStars == event.stars) 0 else event.stars,
                        errorMessage = null
                    )
                }
            }
            is ReviewFeedbackEvent.CommentChanged -> {
                _uiState.update { it.copy(comment = event.comment, errorMessage = null) }
            }
            ReviewFeedbackEvent.Skip -> finishFlow()
            ReviewFeedbackEvent.Submit -> submitReview()
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getUserProfileUseCase()
                .catch { /* Fallback to profile-less review submission. */ }
                .collect { user ->
                    _uiState.update { state ->
                        state.copy(
                            passengerId = user?.id.orEmpty(),
                            passengerName = user?.fullName?.takeIf { it.isNotBlank() }
                                ?: user?.email?.substringBefore("@")
                                ?: "Passenger"
                        )
                    }
                }
        }
    }

    private fun submitReview() {
        val state = _uiState.value
        val passengerId = state.passengerId
        if (passengerId.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "We could not load your profile yet. Please try again.")
            }
            return
        }

        val comment = state.comment.trim()
        val stars = state.selectedStars.coerceIn(0, 5)
        if (stars == 0 && comment.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Add a rating or a comment, or tap Skip.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                reviewRepository.submitReview(
                    SubmitRideReviewRequest(
                        rideId = state.rideId,
                        driverId = state.driverId,
                        driverName = state.driverName,
                        passengerId = passengerId,
                        passengerName = state.passengerName,
                        stars = stars,
                        comment = comment
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isComplete = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "We could not save your review."
                    )
                }
            }
        }
    }

    private fun finishFlow() {
        _uiState.update { it.copy(isComplete = true) }
    }
}

sealed interface ReviewFeedbackEvent {
    data class StarSelected(val stars: Int) : ReviewFeedbackEvent
    data class CommentChanged(val comment: String) : ReviewFeedbackEvent
    data object Submit : ReviewFeedbackEvent
    data object Skip : ReviewFeedbackEvent
}

data class ReviewFeedbackUiState(
    val rideId: String = "",
    val driverId: String = "",
    val driverName: String = "Driver",
    val passengerId: String = "",
    val passengerName: String = "",
    val selectedStars: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
) {
    val canSubmit: Boolean
        get() = selectedStars in 1..5 || comment.trim().isNotBlank()
}
