package com.wheels.app.features.auth.domain.model

import com.wheels.app.core.session.UserRole

data class CreateAccountRequest(
    val fullName: String,
    val username: String,
    val password: String,
    val phone: String,
    val roles: Set<UserRole> = setOf(UserRole.PASSENGER)
)
