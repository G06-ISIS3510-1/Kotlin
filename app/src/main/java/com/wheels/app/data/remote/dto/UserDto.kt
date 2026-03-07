package com.wheels.app.data.remote.dto

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val universityId: String,
    val rating: Double,
    val ridesCompleted: Int,
    val isDriver: Boolean
)
