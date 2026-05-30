package com.wheels.app.features.rides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.rides.domain.model.CreateRideDraftSummary
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
class CreateRideDraftsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rideRepository: RideRepository,
    private val networkMonitor: NetworkMonitor,
    roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRideDraftsUiState())
    val uiState: StateFlow<CreateRideDraftsUiState> = _uiState.asStateFlow()

    val activeRole: StateFlow<UserRole> = roleManager.activeRole

    private var currentDriverId: String? = null
    private var observedDraftDriverId: String? = null

    init {
        observeCurrentDriver()
        observeConnectivity()
    }

    fun onEvent(event: CreateRideDraftsEvent) {
        when (event) {
            CreateRideDraftsEvent.DeleteDraft -> deleteDraft()
            CreateRideDraftsEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun observeCurrentDriver() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                if (user == null) {
                    currentDriverId = null
                    observedDraftDriverId = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            draft = null
                        )
                    }
                    return@collectLatest
                }

                currentDriverId = user.id
                if (observedDraftDriverId == user.id) return@collectLatest
                observedDraftDriverId = user.id
                observeDraft(user.id)
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

    private fun observeDraft(driverId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rideRepository.observeCreateRideDraftSummary(driverId)
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            draft = null,
                            errorMessage = "We could not load your saved ride draft."
                        )
                    }
                }
                .collectLatest { draft ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            draft = draft
                        )
                    }
                }
        }
    }

    private fun deleteDraft() {
        val driverId = currentDriverId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            runCatching {
                rideRepository.clearCreateRideDraft(driverId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        draft = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = throwable.message
                            ?: "We could not delete your saved draft right now."
                    )
                }
            }
        }
    }
}

sealed interface CreateRideDraftsEvent {
    data object DeleteDraft : CreateRideDraftsEvent
    data object DismissError : CreateRideDraftsEvent
}

data class CreateRideDraftsUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isOnline: Boolean = true,
    val draft: CreateRideDraftSummary? = null,
    val errorMessage: String? = null
)
