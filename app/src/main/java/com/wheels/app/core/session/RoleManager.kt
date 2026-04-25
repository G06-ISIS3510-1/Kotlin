package com.wheels.app.core.session

import com.wheels.app.features.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleManager @Inject constructor() {

    private val _activeRole = MutableStateFlow(UserRole.PASSENGER)
    private val _availableRoles = MutableStateFlow(setOf(UserRole.PASSENGER))
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()
    val availableRoles: StateFlow<Set<UserRole>> = _availableRoles.asStateFlow()

    fun syncFromAuthUser(user: AuthUser?) {
        if (user == null) {
            _availableRoles.value = setOf(UserRole.PASSENGER)
            _activeRole.value = UserRole.PASSENGER
            return
        }

        _availableRoles.value = user.roles.ifEmpty { setOf(UserRole.PASSENGER) }
        _activeRole.value = user.activeRole
    }

    fun setRole(role: UserRole, availableRoles: Set<UserRole> = _availableRoles.value) {
        _availableRoles.value = if (availableRoles.isEmpty()) {
            setOf(UserRole.PASSENGER)
        } else {
            availableRoles
        }
        if (role in _availableRoles.value) {
            _activeRole.value = role
        }
    }

    fun hasRole(role: UserRole): Boolean {
        return role in _availableRoles.value
    }
}
