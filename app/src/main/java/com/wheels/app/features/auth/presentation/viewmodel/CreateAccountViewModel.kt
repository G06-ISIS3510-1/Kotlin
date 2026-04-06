package com.wheels.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
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
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun onEvent(event: CreateAccountEvent) {
        when (event) {
            is CreateAccountEvent.FullNameChanged -> updateState(fullName = event.value)
            is CreateAccountEvent.UsernameChanged -> updateState(username = event.value)
            is CreateAccountEvent.PasswordChanged -> updateState(password = event.value)
            is CreateAccountEvent.ConfirmPasswordChanged -> updateState(confirmPassword = event.value)
            is CreateAccountEvent.PhoneChanged -> updateState(phone = event.value)
            is CreateAccountEvent.RoleToggled -> toggleRole(event.role)
            CreateAccountEvent.Submit -> submit()
        }
    }

    private fun updateState(
        fullName: String = _uiState.value.fullName,
        username: String = _uiState.value.username,
        password: String = _uiState.value.password,
        confirmPassword: String = _uiState.value.confirmPassword,
        phone: String = _uiState.value.phone
    ) {
        _uiState.update {
            it.copy(
                fullName = fullName,
                username = username,
                password = password,
                confirmPassword = confirmPassword,
                phone = phone,
                errorMessage = null
            )
        }
    }

    private fun toggleRole(role: UserRole) {
        val currentRoles = _uiState.value.selectedRoles
        val updatedRoles = if (role in currentRoles) {
            currentRoles - role
        } else {
            currentRoles + role
        }

        _uiState.update {
            it.copy(
                selectedRoles = updatedRoles,
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
                        fullName = state.fullName.trim(),
                        username = state.username.trim(),
                        password = state.password,
                        phone = state.phone.trim(),
                        roles = state.selectedRoles
                    )
                )
            ) {
                is Resource.Success -> _uiState.update { it.copy(isSubmitting = false) }
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
        if (state.username.isBlank()) return "Enter your Uniandes username."
        if (state.phone.isBlank()) return "Enter your phone number."
        if (state.selectedRoles.isEmpty()) return "Choose at least one role to continue."
        if (state.password.length < 8) return "Password must be at least 8 characters."
        if (state.password != state.confirmPassword) return "Passwords do not match."
        return null
    }
}

sealed interface CreateAccountEvent {
    data class FullNameChanged(val value: String) : CreateAccountEvent
    data class UsernameChanged(val value: String) : CreateAccountEvent
    data class PasswordChanged(val value: String) : CreateAccountEvent
    data class ConfirmPasswordChanged(val value: String) : CreateAccountEvent
    data class PhoneChanged(val value: String) : CreateAccountEvent
    data class RoleToggled(val role: UserRole) : CreateAccountEvent
    data object Submit : CreateAccountEvent
}

data class CreateAccountUiState(
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val selectedRoles: Set<UserRole> = setOf(UserRole.PASSENGER),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormFilled: Boolean
        get() = fullName.isNotBlank() &&
            username.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            phone.isNotBlank() &&
            selectedRoles.isNotEmpty()
}
