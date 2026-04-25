package com.wheels.app.core.analytics.domain.model

data class UserDestinationInsights(
    val userId: String,
    val topDestinations: List<DestinationInsight>,
    val totalBookingsTracked: Int
)

data class DestinationInsight(
    val destinationName: String,
    val bookingCount: Int,
    val rank: Int
)
