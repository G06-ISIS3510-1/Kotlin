package com.wheels.app.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.domain.usecase.SignOutUseCase
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val roleManager: RoleManager,
    private val driverTrustRepository: DriverTrustRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private var observedTrustUserId: String? = null

    private val _uiState = MutableStateFlow(
        ProfileUiState(activeRole = roleManager.activeRole.value)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadProfile -> Unit
            ProfileEvent.LogOut -> logOut()
            is ProfileEvent.RoleChanged -> {
                roleManager.setRole(event.role)
                _uiState.value = _uiState.value.copy(activeRole = event.role)
            }
            ProfileEvent.ToggleTrustFairnessDarkMode -> {
                _uiState.value = _uiState.value.copy(
                    trustFairnessDarkMode = !_uiState.value.trustFairnessDarkMode
                )
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            getUserProfileUseCase()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trustScoreLoading = false
                    )
                }
                .collect { user ->
                    if (user == null) {
                        observedTrustUserId = null
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            name = "Estudiante Uniandes",
                            email = "",
                            reputationScore = 0.0,
                            ridesCount = 0,
                            trustScore = null,
                            trustScoreLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            name = user.fullName,
                            email = user.email,
                            reputationScore = user.rating,
                            ridesCount = user.ridesCompleted
                        )

                        if (observedTrustUserId != user.id) {
                            observedTrustUserId = user.id
                            observeTrustScore(user.id)
                        }
                    }
                }
        }
    }

    private fun observeTrustScore(userId: String) {
        viewModelScope.launch {
            driverTrustRepository.observeDriverTrustScore(userId)
                .catch {
                    _uiState.value = _uiState.value.copy(trustScoreLoading = false)
                }
                .collect { trustScore ->
                    _uiState.value = _uiState.value.copy(
                        trustScore = trustScore?.reliabilityScore,
                        trustScoreLoading = false
                    )
                }
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            signOutUseCase()
            roleManager.setRole(UserRole.PASSENGER)
        }
    }
}

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data object LogOut : ProfileEvent
    data class RoleChanged(val role: UserRole) : ProfileEvent
    data object ToggleTrustFairnessDarkMode : ProfileEvent
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "Estudiante Uniandes",
    val email: String = "m.gonzalez@uniandes.edu.co",
    val phone: String = "+57 300 123 4567",
    val reputationScore: Double = 0.0,
    val trustScore: Int? = null,
    val trustScoreLoading: Boolean = true,
    val ridesCount: Int = 16,
    val activeRole: UserRole = UserRole.PASSENGER,
    val trustFairnessDarkMode: Boolean = false
)
