package com.wheels.app.features.auth.data.local

import com.wheels.app.features.auth.domain.model.UserSession
import javax.inject.Inject

class SessionManager @Inject constructor() {
    fun saveSession(session: UserSession) {
        TODO("Pending local persistence integration for authenticated sessions.")
    }

    fun getSession(): UserSession? {
        TODO("Pending local persistence integration for session retrieval.")
    }

    fun clearSession() {
        TODO("Pending local persistence integration for session cleanup.")
    }
}
