package com.wheels.app.domain.usecase

import com.wheels.app.domain.model.Booking
import com.wheels.app.domain.repository.RideRepository
import javax.inject.Inject

class BookRideUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(rideId: String, seats: Int): Booking =
        rideRepository.bookRide(rideId, seats)
}
