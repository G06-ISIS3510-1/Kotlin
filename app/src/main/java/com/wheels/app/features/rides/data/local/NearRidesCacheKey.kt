package com.wheels.app.features.rides.data.local

import com.wheels.app.features.rides.domain.model.NearRidesQuery
import kotlin.math.roundToInt

data class NearRidesCacheKey(
    val latBucket: Int,
    val lngBucket: Int,
    val radiusMeters: Int,
    val destinationQuery: String
) {
    companion object {
        fun from(query: NearRidesQuery): NearRidesCacheKey {
            return NearRidesCacheKey(
                latBucket = (query.coordinates.lat * LOCATION_BUCKET_SCALE).roundToInt(),
                lngBucket = (query.coordinates.lng * LOCATION_BUCKET_SCALE).roundToInt(),
                radiusMeters = query.radiusMeters,
                destinationQuery = query.destinationQuery.trim().lowercase()
            )
        }

        private const val LOCATION_BUCKET_SCALE = 1_000.0
    }
}
