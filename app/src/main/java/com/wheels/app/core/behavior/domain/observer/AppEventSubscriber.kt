package com.wheels.app.core.behavior.domain.observer

import com.wheels.app.core.behavior.domain.event.AppEvent

interface AppEventSubscriber {
    suspend fun update(event: AppEvent)
}
