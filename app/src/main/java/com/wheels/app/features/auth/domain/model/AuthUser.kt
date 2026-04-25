package com.wheels.app.features.auth.domain.model

import com.wheels.app.core.session.UserRole

data class AuthUser(
    val uid: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val createdAtMillis: Long?,
    val roles: Set<UserRole>,
    val activeRole: UserRole
)
