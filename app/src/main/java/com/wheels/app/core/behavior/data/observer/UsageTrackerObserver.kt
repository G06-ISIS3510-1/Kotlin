package com.wheels.app.core.behavior.data.observer

import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import com.wheels.app.core.behavior.domain.repository.AppOpenTrackingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageTrackerObserver @Inject constructor(
    private val trackingRepository: AppOpenTrackingRepository
) : AppEventSubscriber {

    override suspend fun update(event: AppEvent) {
        if (event !is AppOpenedEvent) return
        // Persists the raw app-open event so the backend can later build peak
        // usage patterns from the original observations.
        trackingRepository.recordAppOpened(event)
    }
}
