package com.wheels.app.features.profile.domain.model

import com.wheels.app.core.session.UserRole

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val createdAtMillis: Long?,
    val universityId: String,
    val rating: Double,
    val ridesCompleted: Int,
    val roles: Set<UserRole>,
    val activeRole: UserRole
) {
    val isDriver: Boolean
        get() = UserRole.DRIVER in roles
}
