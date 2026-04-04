package com.wheels.app.features.auth.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.usecase.ObserveAuthSessionUseCase
import com.wheels.app.features.auth.domain.usecase.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthSessionViewModel @Inject constructor(
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthSessionUiState())
    val uiState: StateFlow<AuthSessionUiState> = _uiState.asStateFlow()

    init {
        restoreAndObserveSession()
    }

    private fun restoreAndObserveSession() {
        viewModelScope.launch {
            val restoredUser = restoreSessionUseCase()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                authUser = restoredUser
            )

            observeAuthSessionUseCase()
                .catch {
                    _uiState.value = _uiState.value.copy(isLoading = false, authUser = null)
                }
                .collect { authUser ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authUser = authUser
                    )
                }
        }
    }
}

data class AuthSessionUiState(
    val isLoading: Boolean = true,
    val authUser: AuthUser? = null
)
