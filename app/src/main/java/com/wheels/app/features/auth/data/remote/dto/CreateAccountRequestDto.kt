package com.wheels.app.features.auth.data.remote.dto

data class CreateAccountRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String,
    val isDriver: Boolean
)
