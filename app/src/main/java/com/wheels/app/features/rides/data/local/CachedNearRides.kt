package com.wheels.app.features.rides.data.local

import com.wheels.app.features.rides.domain.model.Ride

data class CachedNearRides(
    val rides: List<Ride>,
    val cachedAtMillis: Long
)
