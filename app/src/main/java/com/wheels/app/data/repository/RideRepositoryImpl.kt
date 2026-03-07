package com.wheels.app.data.repository

import com.wheels.app.domain.model.Booking
import com.wheels.app.domain.model.Ride
import com.wheels.app.domain.repository.RideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor() : RideRepository {
    override fun getAvailableRides(): Flow<List<Ride>> = flowOf(
        listOf(
            Ride(
                id = "r_001",
                driverId = "u_001",
                origin = "Universidad de los Andes",
                destination = "Chapinero",
                departureTime = Instant.now().plusSeconds(3600),
                availableSeats = 3,
                pricePerSeat = 8000.0
            )
        )
    )

    override suspend fun bookRide(rideId: String, seats: Int): Booking =
        Booking(
            id = "b_001",
            rideId = rideId,
            passengerId = "u_002",
            seatsReserved = seats,
            status = "PENDING"
        )
}
