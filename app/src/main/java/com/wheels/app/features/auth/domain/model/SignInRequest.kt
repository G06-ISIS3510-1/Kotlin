package com.wheels.app.features.auth.domain.model

data class SignInRequest(
    val email: String,
    val password: String
)
