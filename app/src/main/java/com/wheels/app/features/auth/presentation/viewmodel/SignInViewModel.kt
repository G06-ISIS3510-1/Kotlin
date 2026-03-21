package com.wheels.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.auth.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, errorMessage = null)
            }
            is SignInEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, errorMessage = null)
            }
            SignInEvent.Submit -> submit()
            SignInEvent.ConsumeNavigation -> _uiState.update { it.copy(signedIn = false) }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your university email.") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Enter a valid password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = signInUseCase(SignInRequest(state.email.trim(), state.password))) {
                is Resource.Success -> {
                    roleManager.setRole(UserRole.PASSENGER)
                    _uiState.update { it.copy(isSubmitting = false, signedIn = true) }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = result.message)
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }
}

sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object Submit : SignInEvent
    data object ConsumeNavigation : SignInEvent
}

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val signedIn: Boolean = false
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
