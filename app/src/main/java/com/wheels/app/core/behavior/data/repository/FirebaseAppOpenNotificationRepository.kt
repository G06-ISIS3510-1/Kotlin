package com.wheels.app.core.behavior.data.repository

import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.repository.AppOpenNotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAppOpenNotificationRepository @Inject constructor(
) : AppOpenNotificationRepository {

    override suspend fun prepareNotificationSignal(event: AppOpenedEvent) {
        // The backend now owns user_usage_patterns, so the client-side observer
        // stays intentionally quiet to avoid permission errors and duplicate writes.
        // The event already carries all metadata needed by the backend pipeline.
    }
}
