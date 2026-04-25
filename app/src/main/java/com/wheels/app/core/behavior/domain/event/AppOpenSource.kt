package com.wheels.app.core.behavior.domain.event

enum class AppOpenSource(val storageValue: String) {
    LOGIN("login"),
    SESSION_RESTORE("session_restore"),
    FOREGROUND("foreground")
}
