package com.wheels.app.features.rides.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.repository.RideRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RideRepository {

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

    override fun observeDriverRides(driverId: String): Flow<List<DriverRideRecord>> {
        return callbackFlow {
            val registration = firestore
                .collection(RIDES_COLLECTION)
                .whereEqualTo("driverId", driverId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val rides = snapshot?.documents
                        ?.mapNotNull { document -> document.toDriverRideRecord() }
                        ?.sortedBy { it.departureAt }
                        .orEmpty()

                    trySend(rides)
                }

            awaitClose { registration.remove() }
        }
    }

    override suspend fun publishRide(request: PublishRideRequest): String {
        val rideRef = firestore.collection(RIDES_COLLECTION).document()
        val rideId = rideRef.id

        rideRef.set(
            mapOf(
                "driverId" to request.driverId,
                "driverName" to request.driverName,
                "driverEmail" to request.driverEmail,
                "origin" to request.origin,
                "originSearch" to request.originSearch,
                "destination" to request.destination,
                "destinationSearch" to request.destinationSearch,
                "departureAt" to Timestamp(request.departureAt.epochSecond, request.departureAt.nano),
                "estimatedDurationMinutes" to request.estimatedDurationMinutes,
                "totalSeats" to request.totalSeats,
                "availableSeats" to request.totalSeats,
                "pricePerSeat" to request.pricePerSeat,
                "passengerIds" to request.passengerIds,
                "driverRating" to request.driverRating,
                "onTimeRate" to request.onTimeRate,
                "reviewCount" to request.reviewCount,
                "verifiedByUniversity" to request.verifiedByUniversity,
                "carModel" to request.carModel,
                "licensePlate" to request.licensePlate,
                "notes" to request.notes,
                "status" to RIDE_STATUS_PUBLISHED,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).awaitResult()

        return rideId
    }

    override suspend fun bookRide(rideId: String, seats: Int): Booking =
        Booking(
            id = "b_001",
            rideId = rideId,
            passengerId = "u_002",
            seatsReserved = seats,
            status = "PENDING"
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toDriverRideRecord(): DriverRideRecord? {
        val departureAt = (getTimestamp("departureAt") ?: getTimestamp("scheduledStartAt"))
            ?.toDate()
            ?.toInstant()
            ?: return null

        val estimatedDurationMinutes = getLong("estimatedDurationMinutes")?.toInt()
            ?: getTimestamp("estimatedArrivalAt")
                ?.toDate()
                ?.toInstant()
                ?.let { arrival ->
                    ((arrival.epochSecond - departureAt.epochSecond) / 60).toInt().coerceAtLeast(0)
                }
            ?: DEFAULT_RIDE_DURATION_MINUTES

        return DriverRideRecord(
            id = id,
            driverId = getString("driverId").orEmpty(),
            origin = getString("origin").orEmpty(),
            destination = getString("destination").orEmpty(),
            departureAt = departureAt,
            estimatedDurationMinutes = estimatedDurationMinutes,
            availableSeats = getLong("availableSeats")?.toInt() ?: 0,
            totalSeats = getLong("totalSeats")?.toInt() ?: 0,
            pricePerSeat = getLong("pricePerSeat")?.toInt() ?: 0,
            carModel = getString("carModel").orEmpty(),
            licensePlate = getString("licensePlate").orEmpty(),
            notes = getString("notes") ?: getString("description").orEmpty(),
            driverName = getString("driverName").orEmpty(),
            driverEmail = getString("driverEmail").orEmpty(),
            status = getString("status").orEmpty().ifBlank { RIDE_STATUS_PUBLISHED }
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private companion object {
        const val RIDES_COLLECTION = "rides"
        const val RIDE_STATUS_PUBLISHED = "published"
        const val DEFAULT_RIDE_DURATION_MINUTES = 30
    }
}
