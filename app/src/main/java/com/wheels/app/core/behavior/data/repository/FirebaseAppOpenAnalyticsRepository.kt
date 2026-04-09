package com.wheels.app.core.behavior.data.repository

import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.repository.AppOpenAnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAppOpenAnalyticsRepository @Inject constructor(
) : AppOpenAnalyticsRepository {

    override suspend fun recordAnalytics(event: AppOpenedEvent) {
        // Analytics is intentionally best-effort in the demo and does not need
        // to persist from the client now that the backend owns the real data flow.
        // Keeping the observer preserves the architecture without risking crashes.
    }
}
