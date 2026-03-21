package com.wheels.app.features.analytics.domain.model

data class UserDestinationAnalytics(
    val userId: String,
    val mostFrequentDestinations: List<DestinationVisit>
)
