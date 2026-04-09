package com.wheels.app.core.behavior.domain.usecase

import com.wheels.app.core.behavior.AppOpenEventDebouncer
import com.wheels.app.core.behavior.domain.bus.EventBus
import com.wheels.app.core.behavior.domain.event.AppOpenSource
import com.wheels.app.core.behavior.domain.event.AppOpenedEvent
import com.wheels.app.core.behavior.domain.model.AppOpenIdentity
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class TrackAppOpenUseCase @Inject constructor(
    private val eventBus: EventBus,
    private val appOpenEventDebouncer: AppOpenEventDebouncer
) {
    suspend operator fun invoke(
        identity: AppOpenIdentity,
        source: AppOpenSource
    ) {
        // This feature only tracks authenticated users because the downstream
        // Firebase observers depend on a stable uid/email to persist data.
        if (identity.uid.isBlank() || identity.email.isBlank()) return
        if (!appOpenEventDebouncer.shouldPublish(identity.uid)) return

        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = now.atZone(zoneId)
        // Publish a single domain event that every observer can react to
        // independently without coupling tracking, analytics, and notifications.
        eventBus.publish(
            AppOpenedEvent(
                uid = identity.uid,
                email = identity.email,
                openedAtMillis = now.toEpochMilli(),
                hourOfDay = zonedDateTime.hour,
                minuteOfHour = zonedDateTime.minute,
                dayOfWeek = zonedDateTime.dayOfWeek.value,
                timezone = zoneId.id,
                openSource = source
            )
        )
    }
}
