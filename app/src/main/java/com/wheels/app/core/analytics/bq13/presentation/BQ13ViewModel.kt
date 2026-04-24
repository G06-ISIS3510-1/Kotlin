package com.wheels.app.core.analytics.bq13.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.analytics.bq13.domain.model.BQ13DataSource
import com.wheels.app.core.analytics.bq13.domain.model.BQ13FrequentDestination
import com.wheels.app.core.analytics.bq13.domain.repository.BQ13Repository
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
class BQ13ViewModel @Inject constructor(
    private val repository: BQ13Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BQ13UiState())
    val uiState: StateFlow<BQ13UiState> = _uiState.asStateFlow()

    fun loadFrequentDestinations(userId: String) {
        if (userId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    destinations = emptyList(),
                    emptyStateMessage = "No user is available to load frequent destinations.",
                    errorMessage = null
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.Main.immediate) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    userId = userId,
                    emptyStateMessage = null,
                    errorMessage = null
                )
            }

            val result = runCatching {
                repository.getFrequentDestinations(userId)
            }

            result.onSuccess { bq13Result ->
                val destinations = bq13Result?.destinations.orEmpty()
                val lastUpdatedMillis = bq13Result?.lastUpdatedMillis
                val lastUpdatedLabel = lastUpdatedMillis?.toLastUpdatedLabel()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        destinations = destinations.map { destination -> destination.toUiModel() },
                        isFromCache = bq13Result?.source == BQ13DataSource.CACHE,
                        lastUpdatedLabel = lastUpdatedLabel,
                        emptyStateMessage = if (destinations.isEmpty()) {
                            "No cached frequent destinations are available yet."
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
                        errorMessage = throwable.message ?: "We could not load frequent destinations.",
                        emptyStateMessage = if (it.destinations.isEmpty()) {
                            "No cached frequent destinations are available yet."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    private suspend fun Long.toLastUpdatedLabel(): String = withContext(Dispatchers.IO) {
        "Last updated: ${DateFormat.getDateTimeInstance().format(Date(this@toLastUpdatedLabel))}"
    }

    private fun BQ13FrequentDestination.toUiModel(): BQ13DestinationUiModel {
        return BQ13DestinationUiModel(
            destinationName = destinationName,
            coordinatesLabel = if (latitude != null && longitude != null) {
                "%.5f, %.5f".format(latitude, longitude)
            } else {
                null
            },
            count = count
        )
    }
}

data class BQ13UiState(
    val isLoading: Boolean = false,
    val userId: String = "",
    val destinations: List<BQ13DestinationUiModel> = emptyList(),
    val isFromCache: Boolean = false,
    val lastUpdatedLabel: String? = null,
    val emptyStateMessage: String? = null,
    val errorMessage: String? = null
)

data class BQ13DestinationUiModel(
    val destinationName: String,
    val coordinatesLabel: String?,
    val count: Int
)
