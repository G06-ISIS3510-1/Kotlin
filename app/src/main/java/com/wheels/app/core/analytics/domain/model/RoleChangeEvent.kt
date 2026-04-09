package com.wheels.app.core.analytics.domain.model

data class RoleChangeEvent(
    val uid: String,
    val email: String,
    val oldRole: String,
    val newRole: String,
    val sourceScreen: String,
    val sourceAction: String,
    val changedAtMillis: Long,
    val changedHour: Int,
    val changedMinute: Int,
    val changedMonthKey: String,
    val timezone: String
)
