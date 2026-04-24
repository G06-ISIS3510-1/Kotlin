package com.wheels.app.features.auth.domain.model

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String?
)
