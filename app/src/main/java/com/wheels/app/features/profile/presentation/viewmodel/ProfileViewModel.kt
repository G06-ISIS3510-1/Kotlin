package com.wheels.app.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.core.common.Resource
import com.wheels.app.core.analytics.domain.usecase.TrackRoleChangeUseCase
import com.wheels.app.core.session.RoleManager
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.domain.usecase.RegisterAdditionalRoleUseCase
import com.wheels.app.features.auth.domain.usecase.SignOutUseCase
import com.wheels.app.features.auth.domain.usecase.SwitchActiveRoleUseCase
import com.wheels.app.features.profile.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val roleManager: RoleManager,
    private val networkMonitor: NetworkMonitor,
    private val driverTrustRepository: DriverTrustRepository,
    private val signOutUseCase: SignOutUseCase,
    private val switchActiveRoleUseCase: SwitchActiveRoleUseCase,
    private val registerAdditionalRoleUseCase: RegisterAdditionalRoleUseCase,
    private val trackRoleChangeUseCase: TrackRoleChangeUseCase
) : ViewModel() {

    private var observedTrustUserId: String? = null

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            activeRole = roleManager.activeRole.value,
            availableRoles = roleManager.availableRoles.value
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LoadProfile -> Unit
            ProfileEvent.LogOut -> logOut()
            is ProfileEvent.RoleChanged -> switchRole(event.role)
            is ProfileEvent.RoleUpgradePasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    roleUpgradePassword = event.value,
                    roleUpgradeErrorMessage = null
                )
            }
            ProfileEvent.ConfirmRoleUpgrade -> confirmRoleUpgrade()
            ProfileEvent.DismissRoleUpgradePrompt -> {
                _uiState.value = _uiState.value.copy(
                    roleUpgradeTarget = null,
                    roleUpgradePassword = "",
                    roleUpgradeErrorMessage = null,
                    roleInfoMessage = null
                )
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
            // Keep profile data, active role, and available roles in one UI stream.
            combine(
                getUserProfileUseCase().catch { emit(null) },
                roleManager.activeRole,
                roleManager.availableRoles
            ) { user, activeRole, availableRoles ->
                Triple(user, activeRole, availableRoles)
            }.collect { (user, activeRole, availableRoles) ->
                if (user == null) {
                    observedTrustUserId = null
                    // Use safe placeholders until the cached or remote profile becomes available.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = "Estudiante Uniandes",
                        email = "",
                        phone = "",
                        memberSinceLabel = "Member since --",
                        reputationScore = 0.0,
                        ridesCount = 0,
                        trustScore = null,
                        trustScoreLoading = false,
                        activeRole = activeRole,
                        availableRoles = availableRoles,
                        roleInfoMessage = null
                    )
                } else {
                    val displayName = user.fullName.ifBlank {
                        // Fall back to the email prefix when the backend does not provide a name.
                        user.email.substringBefore("@")
                            .replace('.', ' ')
                            .ifBlank { "Estudiante Uniandes" }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = displayName,
                        email = user.email,
                        phone = user.phone,
                        memberSinceLabel = formatMemberSince(user.createdAtMillis),
                        reputationScore = user.rating,
                        ridesCount = user.ridesCompleted,
                        activeRole = activeRole,
                        availableRoles = availableRoles
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
            // Trust score is loaded separately so failures do not block the profile screen.
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

    private fun switchRole(role: UserRole) {
        // Role switches are gated by connectivity because they depend on remote auth state.
        if (!networkMonitor.isOnline()) {
            _uiState.value = _uiState.value.copy(
                roleActionLoading = false,
                roleUpgradeTarget = null,
                roleUpgradePassword = "",
                roleUpgradeErrorMessage = null,
                roleInfoMessage = "Role changes require an active internet connection."
            )
            return
        }

        if (roleManager.hasRole(role)) {
            val previousRole = _uiState.value.activeRole
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(roleActionLoading = true, roleInfoMessage = null)
                when (val result = switchActiveRoleUseCase(role)) {
                    is Resource.Success -> {
                        roleManager.syncFromAuthUser(result.data)
                        trackRoleChangeUseCase(
                            uid = result.data.uid,
                            email = result.data.email,
                            oldRole = previousRole.storageValue,
                            newRole = role.storageValue,
                            sourceScreen = "Profile",
                            sourceAction = "switch_active_role"
                        )
                        _uiState.value = _uiState.value.copy(
                            roleActionLoading = false,
                            roleInfoMessage = "You are now using Wheels as ${role.displayName.lowercase()}."
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            roleActionLoading = false,
                            roleInfoMessage = result.message
                        )
                    }
                    Resource.Loading -> Unit
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                roleUpgradeTarget = role,
                roleUpgradePassword = "",
                roleUpgradeErrorMessage = null,
                roleInfoMessage = "You are not registered as ${role.displayName.lowercase()} yet. Confirm your password to enable that role."
            )
        }
    }

    private fun confirmRoleUpgrade() {
        val targetRole = _uiState.value.roleUpgradeTarget ?: return
        val password = _uiState.value.roleUpgradePassword
        val previousRole = _uiState.value.activeRole

        // Upgrading a role also requires the server, so we fail fast when offline.
        if (!networkMonitor.isOnline()) {
            _uiState.value = _uiState.value.copy(
                roleActionLoading = false,
                roleUpgradeErrorMessage = "Role changes require an active internet connection."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(roleActionLoading = true, roleUpgradeErrorMessage = null)
            when (val result = registerAdditionalRoleUseCase(targetRole, password)) {
                is Resource.Success -> {
                    roleManager.syncFromAuthUser(result.data)
                    trackRoleChangeUseCase(
                        uid = result.data.uid,
                        email = result.data.email,
                        oldRole = previousRole.storageValue,
                        newRole = targetRole.storageValue,
                        sourceScreen = "Profile",
                        sourceAction = "register_additional_role"
                    )
                    _uiState.value = _uiState.value.copy(
                        roleActionLoading = false,
                        roleUpgradeTarget = null,
                        roleUpgradePassword = "",
                        roleUpgradeErrorMessage = null,
                        roleInfoMessage = "${targetRole.displayName} role enabled successfully."
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        roleActionLoading = false,
                        roleUpgradeErrorMessage = result.message
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            signOutUseCase()
            roleManager.syncFromAuthUser(null)
        }
    }
}

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data object LogOut : ProfileEvent
    data class RoleChanged(val role: UserRole) : ProfileEvent
    data class RoleUpgradePasswordChanged(val value: String) : ProfileEvent
    data object ConfirmRoleUpgrade : ProfileEvent
    data object DismissRoleUpgradePrompt : ProfileEvent
    data object ToggleTrustFairnessDarkMode : ProfileEvent
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "Estudiante Uniandes",
    val email: String = "m.gonzalez@uniandes.edu.co",
    val phone: String = "",
    val memberSinceLabel: String = "Member since --",
    val reputationScore: Double = 0.0,
    val trustScore: Int? = null,
    val trustScoreLoading: Boolean = true,
    val ridesCount: Int = 16,
    val activeRole: UserRole = UserRole.PASSENGER,
    val availableRoles: Set<UserRole> = setOf(UserRole.PASSENGER),
    val trustFairnessDarkMode: Boolean = false,
    val roleInfoMessage: String? = null,
    val roleUpgradeTarget: UserRole? = null,
    val roleUpgradePassword: String = "",
    val roleUpgradeErrorMessage: String? = null,
    val roleActionLoading: Boolean = false
)

private fun formatMemberSince(createdAtMillis: Long?): String {
    if (createdAtMillis == null) return "Member since --"
    val formatter = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    return "Member since ${formatter.format(Date(createdAtMillis))}"
}
