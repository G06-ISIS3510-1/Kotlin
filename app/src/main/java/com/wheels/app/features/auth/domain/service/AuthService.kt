package com.wheels.app.features.auth.domain.service

import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.UserSession

interface AuthService {
    suspend fun loginWithInstitutionalEmail(email: String, password: String): AuthUser

    suspend fun restoreSession(): UserSession?

    suspend fun logout()
}
