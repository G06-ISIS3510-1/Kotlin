package com.wheels.app.features.rides.domain.model

data class NearRidesQuery(
    val coordinates: Coordinates,
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
    val destinationQuery: String = ""
) {
    companion object {
        const val DEFAULT_RADIUS_METERS = 3_000
    }
}
