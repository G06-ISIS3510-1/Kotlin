package com.wheels.app.features.profile.domain.model

import com.wheels.app.core.session.UserRole

// Immutable profile snapshot shared across remote fetch, local cache, and UI.
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
    // Convenience flag used when the UI needs to know if the user can drive.
    val isDriver: Boolean
        get() = UserRole.DRIVER in roles
}
