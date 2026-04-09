package com.wheels.app.core.behavior.data.observer

import com.wheels.app.core.behavior.domain.event.AppEvent
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import com.wheels.app.core.behavior.domain.repository.AppOpenNotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationObserver @Inject constructor(
    private val notificationRepository: AppOpenNotificationRepository
) : AppEventSubscriber {

    override suspend fun update(event: AppEvent) {
        if (event !is AppOpenedEvent) return
        // This observer does not send notifications yet; it only keeps the user
        // usage profile updated with the metadata the backend will need later.
        notificationRepository.prepareNotificationSignal(event)
    }
}
