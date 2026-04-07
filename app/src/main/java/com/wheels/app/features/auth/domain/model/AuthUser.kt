package com.wheels.app.features.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String,
    val role: String,
    val fullName: String
)
