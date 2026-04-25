package com.wheels.app.core.behavior.domain.repository

import com.wheels.app.core.behavior.domain.event.AppOpenedEvent

interface AppOpenTrackingRepository {
    suspend fun recordAppOpened(event: AppOpenedEvent)
}
