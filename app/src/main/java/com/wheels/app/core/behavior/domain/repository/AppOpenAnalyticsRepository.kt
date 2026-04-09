package com.wheels.app.core.behavior.domain.repository

import com.wheels.app.core.behavior.domain.event.AppOpenedEvent

interface AppOpenAnalyticsRepository {
    suspend fun recordAnalytics(event: AppOpenedEvent)
}
