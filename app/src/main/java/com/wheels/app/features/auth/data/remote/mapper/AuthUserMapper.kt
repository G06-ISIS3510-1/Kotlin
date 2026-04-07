package com.wheels.app.features.auth.data.remote.mapper

import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.profile.domain.model.User

fun AuthUser.toProfileUser(): User {
    return User(
        id = uid,
        fullName = fullName.ifBlank { "Wheels User" },
        email = email,
        universityId = email.substringBefore("@").uppercase(),
        rating = 0.0,
        ridesCompleted = 0,
        isDriver = role == "driver"
    )
}
