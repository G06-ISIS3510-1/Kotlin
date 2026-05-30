package com.wheels.app.features.rides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.PendingRidePublish
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import java.time.Instant
import java.util.UUID

@Entity(tableName = "pending_ride_publishes")
data class PendingRidePublishEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val driverName: String,
    val driverEmail: String,
    val origin: String,
    val originSearch: String,
    val originLat: Double?,
    val originLng: Double?,
    val destination: String,
    val destinationSearch: String,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val departureAtEpochMillis: Long,
    val estimatedDurationMinutes: Int,
    val totalSeats: Int,
    val pricePerSeat: Int,
    val carModel: String,
    val licensePlate: String,
    val notes: String,
    val driverRating: Int,
    val onTimeRate: Int,
    val reviewCount: Int,
    val verifiedByUniversity: Boolean,
    val usedCurrentLocationOrigin: Boolean,
    val usedCurrentLocationDestination: Boolean,
    val publishedFromDraft: Boolean,
    val sourceDraftId: String?,
    val retryCount: Int,
    val lastError: String?,
    val createdAtMillis: Long
)

fun PublishRideRequest.toPendingRidePublishEntity(
    id: String = UUID.randomUUID().toString(),
    retryCount: Int = 0,
    lastError: String? = null,
    createdAtMillis: Long = System.currentTimeMillis()
): PendingRidePublishEntity {
    return PendingRidePublishEntity(
        id = id,
        driverId = driverId,
        driverName = driverName,
        driverEmail = driverEmail,
        origin = origin,
        originSearch = originSearch,
        originLat = originCoordinates?.lat,
        originLng = originCoordinates?.lng,
        destination = destination,
        destinationSearch = destinationSearch,
        destinationLat = destinationCoordinates?.lat,
        destinationLng = destinationCoordinates?.lng,
        departureAtEpochMillis = departureAt.toEpochMilli(),
        estimatedDurationMinutes = estimatedDurationMinutes,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        notes = notes,
        driverRating = driverRating,
        onTimeRate = onTimeRate,
        reviewCount = reviewCount,
        verifiedByUniversity = verifiedByUniversity,
        usedCurrentLocationOrigin = usedCurrentLocationOrigin,
        usedCurrentLocationDestination = usedCurrentLocationDestination,
        publishedFromDraft = publishedFromDraft,
        sourceDraftId = sourceDraftId,
        retryCount = retryCount,
        lastError = lastError,
        createdAtMillis = createdAtMillis
    )
}

fun PendingRidePublishEntity.toPublishRideRequest(): PublishRideRequest {
    return PublishRideRequest(
        driverId = driverId,
        driverName = driverName,
        driverEmail = driverEmail,
        origin = origin,
        originSearch = originSearch,
        originCoordinates = originLat?.let { lat ->
            originLng?.let { lng -> Coordinates(lat = lat, lng = lng) }
        },
        destination = destination,
        destinationSearch = destinationSearch,
        destinationCoordinates = destinationLat?.let { lat ->
            destinationLng?.let { lng -> Coordinates(lat = lat, lng = lng) }
        },
        departureAt = Instant.ofEpochMilli(departureAtEpochMillis),
        estimatedDurationMinutes = estimatedDurationMinutes,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        notes = notes,
        driverRating = driverRating,
        onTimeRate = onTimeRate,
        reviewCount = reviewCount,
        verifiedByUniversity = verifiedByUniversity,
        usedCurrentLocationOrigin = usedCurrentLocationOrigin,
        usedCurrentLocationDestination = usedCurrentLocationDestination,
        publishedFromDraft = publishedFromDraft,
        sourceDraftId = sourceDraftId
    )
}

fun PendingRidePublishEntity.toDomain(): PendingRidePublish {
    return PendingRidePublish(
        id = id,
        request = toPublishRideRequest(),
        retryCount = retryCount,
        lastError = lastError,
        createdAtMillis = createdAtMillis
    )
}
