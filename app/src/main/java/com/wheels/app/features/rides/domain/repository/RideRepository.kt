package com.wheels.app.features.rides.domain.repository

import com.wheels.app.core.common.Resource
import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.Ride
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun getAvailableRides(): Flow<List<Ride>>
    fun getNearRides(query: NearRidesQuery): Flow<Resource<List<Ride>>>
    fun observeRide(rideId: String): Flow<Ride?>
    fun observeDriverRides(driverId: String): Flow<List<DriverRideRecord>>
    suspend fun publishRide(request: PublishRideRequest): String
    suspend fun deleteDriverRide(rideId: String)
    suspend fun bookRide(rideId: String, seats: Int): Booking
}
