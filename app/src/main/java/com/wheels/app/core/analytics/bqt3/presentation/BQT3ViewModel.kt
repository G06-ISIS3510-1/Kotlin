package com.wheels.app.core.analytics.bqt3.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageSource
import com.wheels.app.core.analytics.bqt3.domain.repository.BQT3Repository
import com.wheels.app.core.analytics.bqt3.domain.usecase.SyncPendingEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BQT3ViewModel @Inject constructor(
    private val repository: BQT3Repository,
    private val syncPendingEventsUseCase: SyncPendingEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BQT3UiState())
    val uiState: StateFlow<BQT3UiState> = _uiState.asStateFlow()

    fun loadWeeklyUsage(userId: String) {
        runBQT3Action(userId) {
            repository.getCurrentWeekUsage(userId)
        }
    }

    fun registerRidesNearMeUsage(userId: String) {
        runBQT3Action(userId) {
            repository.trackRidesNearMeUsage(userId)
        }
    }

    fun syncPendingEvents() {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            runCatching {
                withContext(Dispatchers.IO) {
                    syncPendingEventsUseCase()
                }
            }
        }
    }

    private fun runBQT3Action(
        userId: String,
        action: suspend () -> com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage
    ) {
        if (userId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No user is available to load Rides Near Me usage.",
                    emptyStateMessage = null
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.Main.immediate) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    userId = userId,
                    errorMessage = null,
                    emptyStateMessage = null
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    action()
                }
            }.onSuccess { weeklyUsage ->
                val lastUpdatedLabel = withContext(Dispatchers.IO) {
                    "Last updated: ${DateFormat.getDateTimeInstance().format(Date(weeklyUsage.lastUpdatedMillis))}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        weekStartDate = weeklyUsage.weekStartDate,
                        usageCount = weeklyUsage.usageCount,
                        isFromCache = weeklyUsage.source == BQT3UsageSource.CACHE,
                        lastUpdatedLabel = lastUpdatedLabel,
                        emptyStateMessage = if (weeklyUsage.usageCount == 0) {
                            "No Rides Near Me usage has been recorded this week."
                        } else {
                            null
                        },
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "We could not load Rides Near Me usage.",
                        emptyStateMessage = if (it.usageCount == 0) {
                            "No cached Rides Near Me usage is available yet."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

data class BQT3UiState(
    val isLoading: Boolean = false,
    val userId: String = "",
    val weekStartDate: String = "",
    val usageCount: Int = 0,
    val isFromCache: Boolean = false,
    val lastUpdatedLabel: String? = null,
    val emptyStateMessage: String? = null,
    val errorMessage: String? = null
)
