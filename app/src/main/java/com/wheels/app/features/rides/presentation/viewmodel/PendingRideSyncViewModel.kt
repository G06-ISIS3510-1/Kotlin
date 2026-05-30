package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.rides.domain.model.PendingRidePublish
import com.wheels.app.features.rides.domain.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PendingRideSyncViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rideRepository: RideRepository,
    private val networkMonitor: NetworkMonitor,
    roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingRideSyncUiState())
    val uiState: StateFlow<PendingRideSyncUiState> = _uiState.asStateFlow()

    val activeRole: StateFlow<UserRole> = roleManager.activeRole

    private var currentDriverId: String? = null
    private var observedDriverId: String? = null

    init {
        observeCurrentDriver()
        observeConnectivity()
    }

    fun onEvent(event: PendingRideSyncEvent) {
        when (event) {
            is PendingRideSyncEvent.RetryPublish -> retryPublish(event.publishId)
            is PendingRideSyncEvent.RemovePublish -> removePublish(event.publishId)
            PendingRideSyncEvent.RetryAll -> retryAll()
            PendingRideSyncEvent.DismissInfo -> _uiState.update { it.copy(infoMessage = null) }
            PendingRideSyncEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun observeCurrentDriver() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                if (user == null) {
                    currentDriverId = null
                    observedDriverId = null
                    _uiState.value = PendingRideSyncUiState(isLoading = false)
                    return@collectLatest
                }

                currentDriverId = user.id
                if (observedDriverId == user.id) return@collectLatest
                observedDriverId = user.id
                observePendingPublishes(user.id)
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.observeIsOnline()
                .distinctUntilChanged()
                .collectLatest { isOnline ->
                    _uiState.update { it.copy(isOnline = isOnline) }
                }
        }
    }

    private fun observePendingPublishes(driverId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rideRepository.observePendingRidePublishes(driverId)
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            pendingPublishes = emptyList(),
                            errorMessage = "We could not load the local sync queue right now."
                        )
                    }
                }
                .collectLatest { publishes ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            pendingPublishes = publishes
                        )
                    }
                }
        }
    }

    private fun retryPublish(publishId: String) {
        if (!_uiState.value.isOnline) {
            _uiState.update {
                it.copy(
                    errorMessage = "You are offline. Reconnect to the internet before retrying this publish.",
                    infoMessage = null
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(retryingPublishId = publishId, errorMessage = null, infoMessage = null) }
            runCatching {
                rideRepository.retryPendingRidePublish(publishId)
            }.onSuccess { synced ->
                _uiState.update {
                    it.copy(
                        retryingPublishId = null,
                        infoMessage = if (synced) {
                            "The ride was published successfully."
                        } else {
                            null
                        },
                        errorMessage = if (!synced) {
                            "The ride could not be synced yet. We kept it in the local queue."
                        } else {
                            null
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        retryingPublishId = null,
                        errorMessage = throwable.message ?: "We could not retry this publish right now."
                    )
                }
            }
        }
    }

    private fun removePublish(publishId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(removingPublishId = publishId, errorMessage = null, infoMessage = null) }
            runCatching {
                rideRepository.deletePendingRidePublish(publishId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        removingPublishId = null,
                        infoMessage = "We removed the pending ride from local storage."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        removingPublishId = null,
                        errorMessage = throwable.message ?: "We could not remove this pending ride."
                    )
                }
            }
        }
    }

    private fun retryAll() {
        val driverId = currentDriverId ?: return
        if (!_uiState.value.isOnline) {
            _uiState.update {
                it.copy(
                    errorMessage = "You are offline. Reconnect to the internet before retrying pending publishes.",
                    infoMessage = null
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRetryingAll = true, errorMessage = null, infoMessage = null) }
            runCatching {
                rideRepository.syncPendingRidePublishes(driverId)
            }.onSuccess { syncedCount ->
                _uiState.update {
                    it.copy(
                        isRetryingAll = false,
                        infoMessage = if (syncedCount > 0) {
                            if (syncedCount == 1) {
                                "We synced 1 pending ride."
                            } else {
                                "We synced $syncedCount pending rides."
                            }
                        } else {
                            "No pending rides could be synced yet."
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRetryingAll = false,
                        errorMessage = throwable.message ?: "We could not retry pending rides right now."
                    )
                }
            }
        }
    }
}

sealed interface PendingRideSyncEvent {
    data class RetryPublish(val publishId: String) : PendingRideSyncEvent
    data class RemovePublish(val publishId: String) : PendingRideSyncEvent
    data object RetryAll : PendingRideSyncEvent
    data object DismissInfo : PendingRideSyncEvent
    data object DismissError : PendingRideSyncEvent
}

data class PendingRideSyncUiState(
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val pendingPublishes: List<PendingRidePublish> = emptyList(),
    val isRetryingAll: Boolean = false,
    val retryingPublishId: String? = null,
    val removingPublishId: String? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
) {
    val failedPublishesCount: Int
        get() = pendingPublishes.count { !it.lastError.isNullOrBlank() }
}
