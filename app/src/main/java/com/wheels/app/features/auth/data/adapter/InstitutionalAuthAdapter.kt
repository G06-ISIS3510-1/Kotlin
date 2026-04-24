package com.wheels.app.features.auth.data.adapter

import com.wheels.app.features.auth.data.local.SessionManager
import com.wheels.app.features.auth.data.remote.AuthClient
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.UserSession
import com.wheels.app.features.auth.domain.service.AuthService
import javax.inject.Inject

class InstitutionalAuthAdapter @Inject constructor(
    private val authClient: AuthClient,
    private val sessionManager: SessionManager
) : AuthService {

    // Adapter isolates the app from provider-specific authentication details.
    override suspend fun loginWithInstitutionalEmail(email: String, password: String): AuthUser {
        TODO("Pending authentication integration and mapping from AuthClient response into AuthUser.")
    }

    override suspend fun restoreSession(): UserSession? {
        TODO("Pending authentication integration and mapping from persisted or remote session state.")
    }

    override suspend fun logout() {
        TODO("Pending authentication integration for clearing provider and local session state.")
    }
}
