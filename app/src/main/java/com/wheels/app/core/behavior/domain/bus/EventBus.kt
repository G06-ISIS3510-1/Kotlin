package com.wheels.app.core.behavior.domain.bus

import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber

interface EventBus {
    fun subscribe(subscriber: AppEventSubscriber)
    suspend fun publish(event: AppEvent)
}
