package com.wheels.app.features.auth.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.behavior.domain.event.AppOpenSource
import com.wheels.app.core.behavior.domain.model.AppOpenIdentity
import com.wheels.app.core.behavior.domain.usecase.TrackAppOpenUseCase
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.domain.usecase.SyncCurrentSessionMetadataUseCase
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
    private val observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val roleManager: RoleManager,
    private val syncCurrentSessionMetadataUseCase: SyncCurrentSessionMetadataUseCase,
    private val trackAppOpenUseCase: TrackAppOpenUseCase
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
            roleManager.syncFromAuthUser(restoredUser)
            if (restoredUser != null) {
                trackAppOpenUseCase(
                    identity = AppOpenIdentity(
                        uid = restoredUser.uid,
                        email = restoredUser.email
                    ),
                    source = AppOpenSource.SESSION_RESTORE
                )
                runCatching { syncCurrentSessionMetadataUseCase() }
            }

            observeAuthSessionUseCase()
                .catch {
                    roleManager.syncFromAuthUser(null)
                    _uiState.value = _uiState.value.copy(isLoading = false, authUser = null)
                }
                .collect { authUser ->
                    roleManager.syncFromAuthUser(authUser)
                    if (authUser != null) {
                        runCatching { syncCurrentSessionMetadataUseCase() }
                    }
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
