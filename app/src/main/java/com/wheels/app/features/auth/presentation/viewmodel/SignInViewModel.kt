package com.wheels.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
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
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.UsernameChanged -> _uiState.update {
                it.copy(username = event.value, errorMessage = null)
            }
            is SignInEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, errorMessage = null)
            }
            SignInEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username or password incorrect.") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username or password incorrect.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = signInUseCase(SignInRequest(state.username.trim(), state.password))) {
                is Resource.Success -> _uiState.update { it.copy(isSubmitting = false) }
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
    data class UsernameChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object Submit : SignInEvent
}

data class SignInUiState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotBlank()
}
