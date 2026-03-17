package com.wheels.app.features.rides.domain.repository

import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.Ride
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun getAvailableRides(): Flow<List<Ride>>
    suspend fun bookRide(rideId: String, seats: Int): Booking
}
