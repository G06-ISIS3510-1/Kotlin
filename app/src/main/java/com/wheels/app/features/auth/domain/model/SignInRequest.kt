package com.wheels.app.features.auth.domain.model

data class SignInRequest(
    val fullName: String,
    val email: String,
    val password: String
)
