package com.wheels.app.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleManager @Inject constructor() {

    private val _activeRole = MutableStateFlow(UserRole.PASSENGER)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

    fun setRole(role: UserRole) {
        _activeRole.value = role
    }
}
