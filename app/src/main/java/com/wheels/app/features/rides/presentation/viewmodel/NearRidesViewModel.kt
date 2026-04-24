package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.location.domain.model.CurrentCoordinates
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.usecase.GetNearRidesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NearRidesViewModel @Inject constructor(
    private val getNearRidesUseCase: GetNearRidesUseCase,
    private val currentLocationProvider: CurrentLocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearRidesUiState())
    val uiState: StateFlow<NearRidesUiState> = _uiState.asStateFlow()

    private var loadNearRidesJob: Job? = null
    private var lastKnownCoordinates: CurrentCoordinates? = null

    fun loadNearRides(destinationQuery: String = _uiState.value.destinationQuery) {
        loadNearRidesJob?.cancel()
        loadNearRidesJob = viewModelScope.launch(Dispatchers.Main.immediate) {
            updateUi {
                it.copy(
                    isLoading = true,
                    destinationQuery = destinationQuery,
                    errorMessage = null
                )
            }

            val coordinatesResult = runCatching {
                withContext(Dispatchers.IO) {
                    currentLocationProvider.getCurrentCoordinates()
                }
            }

            val coordinates = coordinatesResult
                .onSuccess { coordinates -> lastKnownCoordinates = coordinates }
                .getOrElse { throwable ->
                    lastKnownCoordinates ?: run {
                        updateUi {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "We could not read your location."
                            )
                        }
                        return@launch
                    }
                }

            if (coordinatesResult.isFailure) {
                updateUi {
                    it.copy(
                        errorMessage = "Using your last known location while we refresh nearby rides."
                    )
                }
            }

            val query = NearRidesQuery(
                coordinates = Coordinates(lat = coordinates.latitude, lng = coordinates.longitude),
                destinationQuery = destinationQuery
            )

            getNearRidesUseCase(query)
                .catch { throwable ->
                    updateUi {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Near rides are unavailable right now."
                        )
                    }
                }
                .collect { resource ->
                    when (resource) {
                        Resource.Loading -> updateUi { it.copy(isLoading = true) }
                        is Resource.Success -> updateUi {
                            it.copy(
                                isLoading = false,
                                rides = resource.data.toRideCards(),
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> updateUi {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
        }
    }

    private suspend fun updateUi(transform: (NearRidesUiState) -> NearRidesUiState) {
        withContext(Dispatchers.Main.immediate) {
            _uiState.update(transform)
        }
    }
}

data class NearRidesUiState(
    val isLoading: Boolean = false,
    val destinationQuery: String = "",
    val rides: List<RideCardUiModel> = emptyList(),
    val errorMessage: String? = null
)
