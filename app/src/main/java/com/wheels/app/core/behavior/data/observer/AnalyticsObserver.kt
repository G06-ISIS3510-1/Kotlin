package com.wheels.app.core.behavior.data.observer

import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import com.wheels.app.core.behavior.domain.repository.AppOpenAnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsObserver @Inject constructor(
    private val analyticsRepository: AppOpenAnalyticsRepository
) : AppEventSubscriber {

    override suspend fun update(event: AppEvent) {
        if (event !is AppOpenedEvent) return
        // Keeps analytics logging separated from persistence and notification
        // preparation, which is the main reason the Observer split is useful here.
        analyticsRepository.recordAnalytics(event)
    }
}
