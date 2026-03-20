package com.wheels.app.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(activeRole = roleManager.activeRole.value)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadProfile -> Unit
            is ProfileEvent.RoleChanged -> {
                roleManager.setRole(event.role)
                _uiState.value = _uiState.value.copy(activeRole = event.role)
            }
        }
    }
}

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data class RoleChanged(val role: UserRole) : ProfileEvent
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "Estudiante Uniandes",
    val email: String = "m.gonzalez@uniandes.edu.co",
    val phone: String = "+57 300 123 4567",
    val reputationScore: Double = 0.0,
    val activeRole: UserRole = UserRole.PASSENGER
)
