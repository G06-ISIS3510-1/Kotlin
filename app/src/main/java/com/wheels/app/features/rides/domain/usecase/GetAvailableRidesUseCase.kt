package com.wheels.app.features.rides.domain.usecase

import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.repository.RideRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableRidesUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    operator fun invoke(): Flow<List<Ride>> = rideRepository.getAvailableRides()
}
