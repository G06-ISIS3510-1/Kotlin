package com.wheels.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.usecase.ForgotPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.UsernameChanged -> {
                _uiState.update {
                    it.copy(
                        username = event.value,
                        errorMessage = null,
                        successMessage = null
                    )
                }
            }
            ForgotPasswordEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val username = _uiState.value.username.trim()
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your Uniandes username.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }
            when (val result = forgotPasswordUseCase(ForgotPasswordRequest(username))) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Recovery instructions were sent to ${username}@uniandes.edu.co."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }
}

sealed interface ForgotPasswordEvent {
    data class UsernameChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
}

data class ForgotPasswordUiState(
    val username: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canSubmit: Boolean
        get() = username.isNotBlank()
}
