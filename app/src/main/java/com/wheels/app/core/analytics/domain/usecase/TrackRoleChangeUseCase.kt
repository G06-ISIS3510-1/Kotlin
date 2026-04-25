package com.wheels.app.core.analytics.domain.usecase

import com.wheels.app.core.analytics.domain.model.RoleChangeEvent
import com.wheels.app.core.analytics.domain.repository.RoleChangeEventRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TrackRoleChangeUseCase @Inject constructor(
    private val roleChangeEventRepository: RoleChangeEventRepository
) {
    suspend operator fun invoke(
        uid: String,
        email: String,
        oldRole: String,
        newRole: String,
        sourceScreen: String = "Profile",
        sourceAction: String = "role_change"
    ) {
        if (uid.isBlank() || email.isBlank()) return
        if (oldRole.isBlank() || newRole.isBlank()) return

        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = now.atZone(zoneId)

        // Store the timestamp in a reporting-friendly shape so BigQuery or
        // Looker Studio can group by month, hour, or weekday without extra parsing.
        val event = RoleChangeEvent(
            uid = uid,
            email = email,
            oldRole = oldRole,
            newRole = newRole,
            sourceScreen = sourceScreen,
            sourceAction = sourceAction,
            changedAtMillis = now.toEpochMilli(),
            changedHour = zonedDateTime.hour,
            changedMinute = zonedDateTime.minute,
            changedMonthKey = zonedDateTime.format(MONTH_KEY_FORMATTER),
            timezone = zoneId.id
        )

        roleChangeEventRepository.trackRoleChange(event)
    }

    private companion object {
        val MONTH_KEY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
