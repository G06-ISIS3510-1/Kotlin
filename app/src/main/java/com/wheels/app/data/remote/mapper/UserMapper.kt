package com.wheels.app.data.remote.mapper

import com.wheels.app.data.remote.dto.UserDto
import com.wheels.app.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    universityId = universityId,
    rating = rating,
    ridesCompleted = ridesCompleted,
    isDriver = isDriver
)
