package com.wheels.app.features.profile.data.remote.mapper

import com.wheels.app.core.session.UserRole
import com.wheels.app.features.profile.data.remote.dto.UserDto
import com.wheels.app.features.profile.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    phone = "",
    createdAtMillis = null,
    universityId = universityId,
    rating = rating,
    ridesCompleted = ridesCompleted,
    roles = if (isDriver) {
        setOf(UserRole.PASSENGER, UserRole.DRIVER)
    } else {
        setOf(UserRole.PASSENGER)
    },
    activeRole = if (isDriver) UserRole.DRIVER else UserRole.PASSENGER
)
