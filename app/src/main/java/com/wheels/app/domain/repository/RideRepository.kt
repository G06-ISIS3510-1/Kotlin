package com.wheels.app.domain.repository

import com.wheels.app.domain.model.Booking
import com.wheels.app.domain.model.Ride
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun getAvailableRides(): Flow<List<Ride>>
    suspend fun bookRide(rideId: String, seats: Int): Booking
}
