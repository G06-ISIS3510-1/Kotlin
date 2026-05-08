package com.wheels.app.features.rides.domain.repository

import com.wheels.app.core.common.Resource
import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.CreateRideDraft
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.PendingRideAction
import com.wheels.app.features.rides.domain.model.PendingRideActionSyncResult
import com.wheels.app.features.rides.domain.model.PendingRidePublish
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.RideApplication
import com.wheels.app.features.rides.domain.model.Ride
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    fun getAvailableRides(): Flow<List<Ride>>
    fun watchAvailableRides(): Flow<List<Ride>>
    fun getNearRides(query: NearRidesQuery): Flow<Resource<List<Ride>>>
    fun observeRide(rideId: String): Flow<Ride?>
    fun observeDriverRides(driverId: String): Flow<List<DriverRideRecord>>
    fun watchCurrentDriverRide(driverId: String): Flow<Ride?>
    fun watchCurrentPassengerRide(passengerId: String): Flow<Ride?>
    fun watchRideApplications(rideId: String): Flow<List<RideApplication>>
    fun watchPassengerApplication(rideId: String, passengerId: String): Flow<RideApplication?>
    fun observeCreateRideDraft(driverId: String): Flow<CreateRideDraft?>
    fun observePendingRideActions(driverId: String): Flow<List<PendingRideAction>>
    fun observePendingRidePublishes(driverId: String): Flow<List<PendingRidePublish>>
    suspend fun saveCreateRideDraft(draft: CreateRideDraft)
    suspend fun clearCreateRideDraft(driverId: String)
    suspend fun enqueuePendingRideAction(action: PendingRideAction)
    suspend fun enqueueRidePublish(request: PublishRideRequest)
    suspend fun deletePendingRidePublish(id: String)
    suspend fun syncPendingRideActions(driverId: String): List<PendingRideActionSyncResult>
    suspend fun syncPendingRidePublishes(driverId: String): Int
    suspend fun createRide(request: PublishRideRequest): String
    suspend fun publishRide(request: PublishRideRequest): String
    suspend fun applyToRide(
        rideId: String,
        passengerId: String,
        passengerName: String,
        passengerEmail: String
    ): RideApplication
    suspend fun updateRideStatus(rideId: String, status: String)
    suspend fun updatePassengerPaymentStatus(
        rideId: String,
        passengerId: String,
        paymentMethodId: String,
        paymentStatus: String,
        paymentStatusSource: String,
        isPaymentLocked: Boolean,
        status: String,
        statusDetail: String? = null
    )
    suspend fun finishRide(rideId: String)
    suspend fun deleteDriverRide(rideId: String)
    suspend fun bookRide(rideId: String, seats: Int): Booking
}
