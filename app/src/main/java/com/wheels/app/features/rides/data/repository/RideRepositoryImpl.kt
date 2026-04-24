package com.wheels.app.features.rides.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.wheels.app.features.rides.domain.model.Booking
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.data.local.NearRidesLocalCache
import com.wheels.app.features.rides.data.remote.NearRidesRemoteDataSource
import com.wheels.app.core.common.Resource
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.features.rides.domain.repository.RideRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val nearRidesRemoteDataSource: NearRidesRemoteDataSource,
    private val nearRidesLocalCache: NearRidesLocalCache,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher
) : RideRepository {

    override fun getAvailableRides(): Flow<List<Ride>> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val rides = snapshot
                        ?.toAvailableRides()
                        .orEmpty()
                        .filter { ride ->
                            ride.status.equals(RIDE_STATUS_PUBLISHED, ignoreCase = true)
                        }
                        .filter { it.availableSeats > 0 }

                    val reliabilityScores = fetchReliabilityScores(
                        driverIds = rides.map { it.driverId }.distinct()
                    )

                    val enrichedRides = rides
                        .map { ride ->
                            ride.copy(
                                reliabilityScore = reliabilityScores[ride.driverId]
                                    ?: ride.reliabilityScore
                            )
                        }
                        .sortedWith(
                            compareByDescending<Ride> { it.reliabilityScore }
                                .thenBy { it.departureTime }
                        )

                    trySend(enrichedRides)
                }
            }

        awaitClose { registration.remove() }
    }

    override fun getNearRides(query: NearRidesQuery): Flow<Resource<List<Ride>>> = flow {
        val cached = withContext(ioDispatcher) {
            nearRidesLocalCache.get(query)
        }

        if (cached != null) {
            emit(Resource.Success(cached.rides))
        } else {
            emit(Resource.Loading)
        }

        val isOnline = withContext(ioDispatcher) {
            networkMonitor.isOnline()
        }

        if (!isOnline) {
            if (cached == null) {
                emit(Resource.Error("No connection. Connect to the internet to find nearby rides."))
            }
            return@flow
        }

        runCatching {
            nearRidesRemoteDataSource.fetchNearRides(query)
        }.onSuccess { freshRides ->
            withContext(ioDispatcher) {
                nearRidesLocalCache.put(query, freshRides)
            }
            emit(Resource.Success(freshRides))
        }.onFailure { throwable ->
            if (cached == null) {
                emit(
                    Resource.Error(
                        message = throwable.message ?: "Near rides are unavailable right now.",
                        throwable = throwable
                    )
                )
            }
        }
    }

    override fun observeRide(rideId: String): Flow<Ride?> = callbackFlow {
        val registration = firestore
            .collection(RIDES_COLLECTION)
            .document(rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    val baseRide = snapshot?.toAvailableRide()
                    if (baseRide == null) {
                        trySend(null)
                        return@launch
                    }

                    val reliabilityScore = fetchReliabilityScore(baseRide.driverId)
                    trySend(
                        baseRide.copy(
                            reliabilityScore = reliabilityScore ?: baseRide.reliabilityScore
                        )
                    )
                }
            }

        awaitClose { registration.remove() }
    }

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
                "originCoordinates" to request.originCoordinates?.toGeoPoint(),
                "destination" to request.destination,
                "destinationSearch" to request.destinationSearch,
                "destinationCoordinates" to request.destinationCoordinates?.toGeoPoint(),
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

    override suspend fun deleteDriverRide(rideId: String) {
        firestore.collection(RIDES_COLLECTION).document(rideId).delete().awaitResult()
    }

    override suspend fun bookRide(rideId: String, seats: Int): Booking =
        Booking(
            id = "b_001",
            rideId = rideId,
            passengerId = "u_002",
            seatsReserved = seats,
            status = "PENDING"
        )

    private suspend fun QuerySnapshot.toAvailableRides(): List<Ride> {
        return documents.mapNotNull { document -> document.toAvailableRide() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAvailableRide(): Ride? {
        val departureTime = (getTimestamp("departureAt") ?: getTimestamp("scheduledStartAt"))
            ?.toDate()
            ?.toInstant()
            ?: return null

        val destination = getString("destination").orEmpty()
        val totalSeats = getLong("totalSeats")?.toInt() ?: 0
        val availableSeats = getLong("availableSeats")?.toInt() ?: totalSeats
        val estimatedDurationMinutes = getLong("estimatedDurationMinutes")?.toInt()
            ?: DEFAULT_RIDE_DURATION_MINUTES
        val driverRating = getDouble("driverRating")
            ?: getLong("driverRating")?.toDouble()
            ?: DEFAULT_DRIVER_RATING
        val reviewCount = getLong("reviewCount")?.toInt() ?: 0
        val punctualityRate = getLong("onTimeRate")?.toInt() ?: DEFAULT_PUNCTUALITY_RATE
        val pricePerSeat = getLong("pricePerSeat")?.toDouble()
            ?: getDouble("pricePerSeat")
            ?: 0.0

        return Ride(
            id = id,
            driverId = getString("driverId").orEmpty(),
            driverName = getString("driverName").orEmpty(),
            driverEmail = getString("driverEmail").orEmpty(),
            driverRating = driverRating,
            reviewCount = reviewCount,
            reliabilityScore = DEFAULT_RELIABILITY_SCORE,
            status = getString("status").orEmpty().ifBlank { RIDE_STATUS_PUBLISHED },
            origin = getString("origin").orEmpty(),
            originCoordinates = getCoordinates("originCoordinates"),
            destination = destination,
            destinationCoordinates = getCoordinates("destinationCoordinates"),
            destinationArea = destination.substringAfterLast(",").trim().ifBlank { destination },
            departureTime = departureTime,
            estimatedDurationMinutes = estimatedDurationMinutes,
            availableSeats = availableSeats,
            totalSeats = totalSeats,
            pricePerSeat = pricePerSeat,
            punctualityRate = punctualityRate,
            isHabitRide = false,
            carModel = getString("carModel").orEmpty(),
            licensePlate = getString("licensePlate").orEmpty(),
            notes = getString("notes") ?: getString("description").orEmpty(),
            verifiedByUniversity = getBoolean("verifiedByUniversity") ?: false
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDriverRideRecord(): DriverRideRecord? {
        val status = getString("status").orEmpty().ifBlank { RIDE_STATUS_PUBLISHED }
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
            originCoordinates = getCoordinates("originCoordinates"),
            destination = getString("destination").orEmpty(),
            destinationCoordinates = getCoordinates("destinationCoordinates"),
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
            status = status
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
    }

    private suspend fun fetchReliabilityScores(driverIds: List<String>): Map<String, Int> {
        return driverIds.associateWith { driverId ->
            fetchReliabilityScore(driverId) ?: DEFAULT_RELIABILITY_SCORE
        }
    }

    private suspend fun fetchReliabilityScore(driverId: String): Int? {
        if (driverId.isBlank()) return null

        val snapshot = firestore
            .collection(TRUST_SCORES_COLLECTION)
            .document(driverId)
            .get()
            .awaitResult()

        return snapshot.getLong("reliabilityScore")?.toInt()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getCoordinates(field: String): Coordinates? {
        getGeoPoint(field)?.let { geoPoint ->
            return geoPoint.toCoordinates()
        }

        val coordinateMap = get(field)
        if (coordinateMap !is Map<*, *>) {
            return null
        }

        val latitude = (coordinateMap["lat"] as? Number)?.toDouble()
            ?: (coordinateMap["latitude"] as? Number)?.toDouble()
        val longitude = (coordinateMap["lng"] as? Number)?.toDouble()
            ?: (coordinateMap["longitude"] as? Number)?.toDouble()

        return if (latitude != null && longitude != null) {
            Coordinates(lat = latitude, lng = longitude)
        } else {
            null
        }
    }

    private fun Coordinates.toGeoPoint(): GeoPoint {
        return GeoPoint(lat, lng)
    }

    private fun GeoPoint.toCoordinates(): Coordinates {
        return Coordinates(
            lat = latitude,
            lng = longitude
        )
    }

    private companion object {
        const val RIDES_COLLECTION = "rides"
        const val TRUST_SCORES_COLLECTION = "trustScores"
        const val RIDE_STATUS_PUBLISHED = "published"
        const val RIDE_STATUS_CANCELED = "canceled"
        const val DEFAULT_RIDE_DURATION_MINUTES = 30
        const val DEFAULT_RELIABILITY_SCORE = 100
        const val DEFAULT_PUNCTUALITY_RATE = 100
        const val DEFAULT_DRIVER_RATING = 5.0
    }
}
