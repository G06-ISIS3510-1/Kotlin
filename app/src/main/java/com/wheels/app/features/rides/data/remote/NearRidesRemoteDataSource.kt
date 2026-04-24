package com.wheels.app.features.rides.data.remote

import com.wheels.app.features.rides.data.remote.api.RideApi
import com.wheels.app.features.rides.data.remote.mapper.toDomain
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.Ride
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearRidesRemoteDataSource @Inject constructor(
    private val rideApi: RideApi,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchNearRides(query: NearRidesQuery): List<Ride> = withContext(ioDispatcher) {
        rideApi.getNearRides(
            latitude = query.coordinates.lat,
            longitude = query.coordinates.lng,
            radiusMeters = query.radiusMeters,
            destinationQuery = query.destinationQuery.ifBlank { null }
        ).map { dto -> dto.toDomain() }
    }
}
