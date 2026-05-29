package com.wheels.app.features.reviews.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.wheels.app.features.reviews.data.local.PendingRideReviewEntity
import com.wheels.app.features.reviews.data.local.PendingRideReviewLocalStore
import com.wheels.app.features.reviews.data.local.buildPendingRideReviewKey
import com.wheels.app.features.reviews.data.local.toPendingRideReviewEntity
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import com.wheels.app.features.reviews.sync.RideReviewSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

private const val OFFLINE_REVIEW_QUEUE_NOTICE =
    "No connection right now. Your review was saved locally and will stay queued until you're back online."

/**
 * Coordinates the review screen from start to finish.
 *
 * The ViewModel decides whether to publish the review immediately or save it locally first,
 * and it also keeps the small connection/sync banners aligned with that state.
 */
@HiltViewModel
class ReviewFeedbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val reviewRepository: RideReviewRepository,
    private val pendingRideReviewStore: PendingRideReviewLocalStore,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
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

    private var currentPassengerId: String = ""
    // Used to watch the exact queue item that belongs to this ride/passenger pair.
    private var currentReviewKey: String? = null
    // These flags let us tell the difference between "still queued" and "already synced".
    private var hasQueuedReview: Boolean = false
    private var lastQueuedReview: PendingRideReviewEntity? = null
    private var suppressNextQueueRemovalNotice: Boolean = false
    private var isOnline: Boolean = true
    private var pendingReviewObserverJob: Job? = null

    init {
        // Keep profile and connectivity separate:
        // the profile gives us a stable passenger key, and connectivity decides whether we submit now or queue first.
        observeCurrentUser()
        observeConnectivity()
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
                    // We still let the user write feedback if the profile is late,
                    // but we need the passenger ID before we can save the queued review.
                    currentPassengerId = user?.id.orEmpty()
                    _uiState.update { state ->
                        state.copy(
                            passengerId = user?.id.orEmpty(),
                            passengerName = user?.fullName?.takeIf { it.isNotBlank() }
                                ?: user?.email?.substringBefore("@")
                                ?: "Passenger"
                        )
                    }
                    syncPendingReviewObserver()
                }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            // This is only used for messaging and to choose the submit path.
            // The actual offline persistence happens in queueReview().
            isOnline = networkMonitor.isOnline()
            refreshConnectionNotice()
            scheduleReviewSyncWorkIfNeeded()

            networkMonitor.observeIsOnline()
                .collectLatest { online ->
                    isOnline = online
                    refreshConnectionNotice()
                    scheduleReviewSyncWorkIfNeeded()
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

        // Validation happens before any I/O so the user gets immediate feedback.
        val comment = state.comment.trim()
        val stars = state.selectedStars.coerceIn(0, 5)
        if (stars == 0 && comment.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Add a rating or a comment, or tap Skip.")
            }
            return
        }

        viewModelScope.launch {
            // The coroutine keeps the UI responsive while we decide whether to post or queue.
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val request = SubmitRideReviewRequest(
                rideId = state.rideId,
                driverId = state.driverId,
                driverName = state.driverName,
                passengerId = passengerId,
                passengerName = state.passengerName,
                stars = stars,
                comment = comment
            )

            if (!isOnline) {
                // Local-first path:
                // save immediately, leave the screen, and let WorkManager replay the review later.
                queueReview(request)
                return@launch
            }

            runCatching {
                reviewRepository.submitReview(request)
            }.onSuccess {
                // If the review was already queued locally, remove the stale copy so the queue state stays accurate.
                suppressNextQueueRemovalNotice = true
                runCatching {
                    pendingRideReviewStore.deletePendingRideReview(
                        buildPendingRideReviewKey(state.rideId, passengerId)
                    )
                }
                hasQueuedReview = false
                lastQueuedReview = null
                currentReviewKey = null
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isComplete = true,
                        connectionNotice = null,
                        syncNotice = null,
                        queuedNotice = null
                    )
                }
            }.onFailure { throwable ->
                if (throwable.shouldQueueReview() || !networkMonitor.isOnline()) {
                    queueReview(request)
                } else {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "We could not save your review."
                        )
                    }
                }
            }
        }
    }

    private fun finishFlow() {
        // Skip and submit both leave the screen through the same "done" path.
        _uiState.update {
            it.copy(
                isComplete = true,
                connectionNotice = null,
                syncNotice = null,
                queuedNotice = null
            )
        }
    }

    private fun syncPendingReviewObserver() {
        val passengerId = currentPassengerId
        if (passengerId.isBlank()) {
            currentReviewKey = null
            hasQueuedReview = false
            lastQueuedReview = null
            suppressNextQueueRemovalNotice = false
            pendingReviewObserverJob?.cancel()
            pendingReviewObserverJob = null
            refreshConnectionNotice()
            return
        }

        val nextReviewKey = buildPendingRideReviewKey(_uiState.value.rideId, passengerId)
        if (currentReviewKey == nextReviewKey) return

        pendingReviewObserverJob?.cancel()
        currentReviewKey = nextReviewKey
        pendingReviewObserverJob = viewModelScope.launch {
            pendingRideReviewStore.observePendingRideReview(nextReviewKey)
                .catch { /* Keep the screen functional even if the queue observer fails. */ }
                .collect { pendingReview ->
                    val wasQueued = hasQueuedReview
                    hasQueuedReview = pendingReview != null
                    if (pendingReview != null) {
                        // A local row exists, so the review is still waiting to be published.
                        lastQueuedReview = pendingReview
                        _uiState.update { it.copy(syncNotice = null) }
                        scheduleReviewSyncWorkIfNeeded()
                    } else {
                        val shouldShowSyncNotice = wasQueued && !suppressNextQueueRemovalNotice
                        suppressNextQueueRemovalNotice = false
                        if (shouldShowSyncNotice) {
                            // Once the local row disappears, the worker has already published it remotely.
                            _uiState.update {
                                it.copy(
                                    syncNotice = if (lastQueuedReview?.comment?.isNotBlank() == true) {
                                        "Your comment was synced."
                                    } else {
                                        "Your review was synced."
                                    },
                                    connectionNotice = null,
                                    queuedNotice = null
                                )
                            }
                            lastQueuedReview = null
                        }
                    }

                    refreshConnectionNotice()
                }
        }
    }

    private suspend fun queueReview(request: SubmitRideReviewRequest) {
        runCatching {
            // Persist first, then schedule sync.
            // If the app closes right now, the review is still recoverable from local storage.
            val pendingReview = request.toPendingRideReviewEntity(
                passengerName = request.passengerName
            )
            pendingRideReviewStore.upsertPendingRideReview(pendingReview)
            hasQueuedReview = true
            lastQueuedReview = pendingReview
            suppressNextQueueRemovalNotice = false
        }.onSuccess {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    isComplete = true,
                    errorMessage = null,
                    connectionNotice = null,
                    syncNotice = null,
                    queuedNotice = OFFLINE_REVIEW_QUEUE_NOTICE
                )
            }
            refreshConnectionNotice()
            scheduleReviewSyncWorkIfNeeded()
        }.onFailure { throwable ->
            // If the local save fails, the review was not protected anywhere, so we must surface an error.
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = throwable.message ?: "We could not save your review."
                )
            }
        }
    }

    private fun scheduleReviewSyncWorkIfNeeded() {
        if (!hasQueuedReview) return

        viewModelScope.launch {
            // WorkManager owns the retry policy and only runs when the network is available.
            // We only enqueue the work request here; the actual replay happens in the worker.
            runCatching {
                enqueueReviewSyncWork()
            }
        }
    }

    private fun Throwable.shouldQueueReview(): Boolean {
        return when (this) {
            is FirebaseNetworkException -> true
            is FirebaseFirestoreException -> code in setOf(
                FirebaseFirestoreException.Code.ABORTED,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                FirebaseFirestoreException.Code.UNAVAILABLE
            )
            else -> false
        }
    }

    private fun enqueueReviewSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RideReviewSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RideReviewSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun refreshConnectionNotice() {
        // This banner explains what will happen, but it never blocks typing or submission.
        val connectionNotice = when {
            !isOnline -> "No connection right now. You can still write your review, and we'll send it automatically when you're back online."
            hasQueuedReview -> "Your review is queued and will sync automatically."
            else -> null
        }

        _uiState.update { state ->
            state.copy(
                connectionNotice = connectionNotice
            )
        }
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
    val connectionNotice: String? = null,
    val syncNotice: String? = null,
    val queuedNotice: String? = null,
    val isComplete: Boolean = false
) {
    val canSubmit: Boolean
        get() = selectedStars in 1..5 || comment.trim().isNotBlank()
}
