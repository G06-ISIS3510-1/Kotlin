package com.wheels.app.core.behavior.domain.repository

import com.wheels.app.core.behavior.domain.event.AppOpenedEvent

interface AppOpenNotificationRepository {
    suspend fun prepareNotificationSignal(event: AppOpenedEvent)
}
