package com.wheels.app.features.rides.domain.usecase

import com.wheels.app.core.common.Resource
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.repository.RideRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetNearRidesUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    operator fun invoke(query: NearRidesQuery): Flow<Resource<List<Ride>>> {
        return rideRepository.getNearRides(query)
    }
}
