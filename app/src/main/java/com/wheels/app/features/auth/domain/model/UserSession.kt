package com.wheels.app.features.auth.domain.model

data class UserSession(
    val userId: String,
    val token: String,
    val isActive: Boolean
)
