package com.wheels.app.features.auth.domain.model

import com.wheels.app.core.session.UserRole

data class CreateAccountRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: UserRole = UserRole.PASSENGER
)
