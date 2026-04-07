package com.wheels.app.features.rides.domain.model

data class BehavioralNudge(
    val title: String,
    val message: String,
    val plannedReminderOffsetsHours: List<Int> = listOf(12, 3)
)
