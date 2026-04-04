package com.wheels.app.features.auth.data.remote

import javax.inject.Inject

class AuthClient @Inject constructor() {
    suspend fun authenticateInstitutionalUser(email: String, password: String): String {
        throw NotImplementedError("Pending authentication integration for institutional login.")
    }

    suspend fun fetchCurrentSession(): String? {
        throw NotImplementedError("Pending authentication integration for session recovery.")
    }

    suspend fun clearSession() {
        throw NotImplementedError("Pending authentication integration for logout.")
    }
}
