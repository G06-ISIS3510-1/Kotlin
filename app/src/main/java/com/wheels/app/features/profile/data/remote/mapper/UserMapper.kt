package com.wheels.app.features.profile.data.remote.mapper

import com.wheels.app.features.profile.data.remote.dto.UserDto
import com.wheels.app.features.profile.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    universityId = universityId,
    rating = rating,
    ridesCompleted = ridesCompleted,
    isDriver = isDriver
)
