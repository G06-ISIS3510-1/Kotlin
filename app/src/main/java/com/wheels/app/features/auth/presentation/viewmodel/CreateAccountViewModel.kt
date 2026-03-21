package com.wheels.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.usecase.CreateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun onEvent(event: CreateAccountEvent) {
        when (event) {
            is CreateAccountEvent.FullNameChanged -> updateState(fullName = event.value)
            is CreateAccountEvent.EmailChanged -> updateState(email = event.value)
            is CreateAccountEvent.PasswordChanged -> updateState(password = event.value)
            is CreateAccountEvent.ConfirmPasswordChanged -> updateState(confirmPassword = event.value)
            is CreateAccountEvent.PhoneChanged -> updateState(phone = event.value)
            CreateAccountEvent.Submit -> submit()
            CreateAccountEvent.ConsumeNavigation -> {
                _uiState.update { it.copy(accountCreated = false) }
            }
        }
    }

    private fun updateState(
        fullName: String = _uiState.value.fullName,
        email: String = _uiState.value.email,
        password: String = _uiState.value.password,
        confirmPassword: String = _uiState.value.confirmPassword,
        phone: String = _uiState.value.phone
    ) {
        _uiState.update {
            it.copy(
                fullName = fullName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                phone = phone,
                errorMessage = null
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = createAccountUseCase(
                    CreateAccountRequest(
                        state.fullName.trim(),
                        state.email.trim(),
                        state.password,
                        state.phone.trim(),
                        UserRole.PASSENGER
                    )
                )
            ) {
                is Resource.Success -> {
                    roleManager.setRole(UserRole.PASSENGER)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            accountCreated = true
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

    private fun validate(state: CreateAccountUiState): String? {
        if (state.fullName.isBlank()) return "Enter your full name."
        if (state.email.isBlank()) return "Enter your university email."
        if (!state.email.contains("@") || !state.email.contains(".")) {
            return "Enter a valid email."
        }
        if (state.phone.isBlank()) return "Enter your phone number."
        if (state.password.length < 8) return "Password must be at least 8 characters."
        if (state.password != state.confirmPassword) return "Passwords do not match."
        return null
    }
}

sealed interface CreateAccountEvent {
    data class FullNameChanged(val value: String) : CreateAccountEvent
    data class EmailChanged(val value: String) : CreateAccountEvent
    data class PasswordChanged(val value: String) : CreateAccountEvent
    data class ConfirmPasswordChanged(val value: String) : CreateAccountEvent
    data class PhoneChanged(val value: String) : CreateAccountEvent
    data object Submit : CreateAccountEvent
    data object ConsumeNavigation : CreateAccountEvent
}

data class CreateAccountUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val accountCreated: Boolean = false
) {
    val isFormFilled: Boolean
        get() = fullName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            phone.isNotBlank()
}
