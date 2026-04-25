package com.wheels.app.core.behavior.domain.event

data class AppOpenedEvent(
    val uid: String,
    val email: String,
    val openedAtMillis: Long,
    val hourOfDay: Int,
    val minuteOfHour: Int,
    val dayOfWeek: Int,
    val timezone: String,
    val openSource: AppOpenSource
) : AppEvent
