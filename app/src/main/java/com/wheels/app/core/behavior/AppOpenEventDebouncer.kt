package com.wheels.app.core.behavior

import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenEventDebouncer @Inject constructor() {

    private val lastPublishedByUser = ConcurrentHashMap<String, Long>()

    fun shouldPublish(uid: String): Boolean {
        if (uid.isBlank()) return false

        val nowElapsed = SystemClock.elapsedRealtime()
        val lastPublishedAt = lastPublishedByUser[uid]
        if (lastPublishedAt != null && nowElapsed - lastPublishedAt < DEBOUNCE_WINDOW_MILLIS) {
            return false
        }

        lastPublishedByUser[uid] = nowElapsed
        return true
    }

    private companion object {
        // A short window prevents login/session restore and foreground from
        // double-counting the same launch cycle while keeping later opens valid.
        const val DEBOUNCE_WINDOW_MILLIS = 5_000L
    }
}
