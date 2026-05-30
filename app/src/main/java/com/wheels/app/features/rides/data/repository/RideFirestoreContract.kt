package com.wheels.app.features.rides.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import com.wheels.app.features.rides.domain.model.PublishRideRequest
import com.wheels.app.features.rides.domain.model.Ride
import com.wheels.app.features.rides.domain.model.RideApplication
import java.time.Instant

internal const val RIDE_STATUS_OPEN = "open"
internal const val RIDE_STATUS_PUBLISHED = "published"
internal const val RIDE_STATUS_IN_PROGRESS = "in_progress"
internal const val RIDE_STATUS_COMPLETED = "completed"
internal const val RIDE_STATUS_CANCELLED = "cancelled"

internal const val APPLICATION_STATUS_APPLIED = "applied"
internal const val APPLICATION_STATUS_APPROVED = "approved"
internal const val APPLICATION_STATUS_REJECTED = "rejected"

internal const val PAYMENT_METHOD_PENDING_SELECTION = "pending_selection"
internal const val PAYMENT_METHOD_BANK_TRANSFER = "bank_transfer"

internal const val PAYMENT_STATUS_PENDING = "pending"
internal const val PAYMENT_STATUS_PAID = "paid"
internal const val PAYMENT_STATUS_UNPAID = "unpaid"

internal const val PAYMENT_MIRROR_STATUS_PENDING = "pending"
internal const val PAYMENT_MIRROR_STATUS_APPROVED = "approved"
internal const val PAYMENT_MIRROR_STATUS_REJECTED = "rejected"

internal const val PAYMENT_STATUS_SOURCE_APPLICATION_CREATED = "application_created"
internal const val PAYMENT_STATUS_SOURCE_RIDE_FINISHED = "ride_finished"

internal sealed interface ApplyRideDecision {
    data object NoOp : ApplyRideDecision
    data class Apply(
        val application: RideApplication,
        val paymentMethodId: String,
        val paymentStatus: String,
        val paymentStatusSource: String,
        val paymentMirrorStatus: String,
        val paymentMirrorStatusDetail: String
    ) : ApplyRideDecision
}

internal sealed interface RideStatusTransitionDecision {
    data object Allowed : RideStatusTransitionDecision
    data class Rejected(val reason: String) : RideStatusTransitionDecision
}

internal fun normalizeRideReadStatus(rawStatus: String?): String {
    return when (rawStatus?.trim()?.lowercase()) {
        RIDE_STATUS_PUBLISHED -> RIDE_STATUS_OPEN
        RIDE_STATUS_IN_PROGRESS -> RIDE_STATUS_IN_PROGRESS
        RIDE_STATUS_COMPLETED -> RIDE_STATUS_COMPLETED
        RIDE_STATUS_CANCELLED,
        "canceled" -> RIDE_STATUS_CANCELLED
        else -> rawStatus?.trim()?.lowercase().orEmpty().ifBlank { RIDE_STATUS_OPEN }
    }
}

internal fun isPassengerHomeRideVisible(rawStatus: String?): Boolean {
    return when (normalizeRideReadStatus(rawStatus)) {
        RIDE_STATUS_OPEN,
        RIDE_STATUS_IN_PROGRESS,
        RIDE_STATUS_COMPLETED -> true
        else -> false
    }
}

internal fun normalizeRideWriteStatus(rawStatus: String): String {
    return when (rawStatus.trim().lowercase()) {
        RIDE_STATUS_PUBLISHED -> RIDE_STATUS_OPEN
        "canceled" -> RIDE_STATUS_CANCELLED
        else -> rawStatus.trim().lowercase()
    }
}

internal fun validateRideStatusTransition(currentStatus: String, nextStatus: String): RideStatusTransitionDecision {
    val normalizedCurrent = normalizeRideReadStatus(currentStatus)
    val normalizedNext = normalizeRideWriteStatus(nextStatus)

    return when (normalizedCurrent) {
        RIDE_STATUS_OPEN -> when (normalizedNext) {
            RIDE_STATUS_IN_PROGRESS, RIDE_STATUS_CANCELLED -> RideStatusTransitionDecision.Allowed
            else -> RideStatusTransitionDecision.Rejected(
                "You can only move an open ride to in_progress or cancelled."
            )
        }
        RIDE_STATUS_IN_PROGRESS -> when (normalizedNext) {
            RIDE_STATUS_COMPLETED, RIDE_STATUS_CANCELLED -> RideStatusTransitionDecision.Allowed
            else -> RideStatusTransitionDecision.Rejected(
                "You can only move an in-progress ride to completed or cancelled."
            )
        }
        RIDE_STATUS_COMPLETED,
        RIDE_STATUS_CANCELLED -> RideStatusTransitionDecision.Rejected(
            "Finished rides cannot change status again."
        )
        else -> RideStatusTransitionDecision.Rejected("Unsupported ride status transition.")
    }
}

internal fun decideRideApplication(
    rideId: String,
    rideData: Map<String, Any?>,
    passengerId: String,
    passengerName: String,
    passengerEmail: String,
    existingApplicationExists: Boolean,
    appliedAt: Instant = Instant.now()
): ApplyRideDecision {
    if (existingApplicationExists) {
        return ApplyRideDecision.NoOp
    }

    val driverId = readString(rideData, "driverId").orEmpty()
    val normalizedStatus = normalizeRideReadStatus(readString(rideData, "status"))
    val availableSeats = readInt(rideData, "availableSeats")
        ?: readInt(rideData, "totalSeats")
        ?: readInt(rideData, "seats")
        ?: 0
    val paymentOption = readString(rideData, "paymentOption")
        .orEmpty()
        .trim()
        .lowercase()
        .ifBlank { "card" }

    if (passengerId == driverId) {
        return ApplyRideDecision.Apply(
            application = RideApplication(
                id = passengerId,
                rideId = rideId,
                passengerId = passengerId,
                passengerName = passengerName,
                passengerEmail = passengerEmail,
                status = APPLICATION_STATUS_REJECTED,
                paymentMethod = paymentOption,
                paymentStatus = PAYMENT_STATUS_UNPAID,
                isPaymentLocked = false,
                paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
                statusDetail = "driver_cannot_apply",
                appliedAt = appliedAt,
                updatedAt = appliedAt
            ),
            paymentMethodId = paymentOption,
            paymentStatus = PAYMENT_STATUS_UNPAID,
            paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
            paymentMirrorStatus = PAYMENT_MIRROR_STATUS_REJECTED,
            paymentMirrorStatusDetail = "driver_cannot_apply"
        )
    }

    if (normalizedStatus != RIDE_STATUS_OPEN) {
        return ApplyRideDecision.Apply(
            application = RideApplication(
                id = passengerId,
                rideId = rideId,
                passengerId = passengerId,
                passengerName = passengerName,
                passengerEmail = passengerEmail,
                status = APPLICATION_STATUS_REJECTED,
                paymentMethod = paymentOption,
                paymentStatus = PAYMENT_STATUS_UNPAID,
                isPaymentLocked = false,
                paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
                statusDetail = "ride_not_open",
                appliedAt = appliedAt,
                updatedAt = appliedAt
            ),
            paymentMethodId = paymentOption,
            paymentStatus = PAYMENT_STATUS_UNPAID,
            paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
            paymentMirrorStatus = PAYMENT_MIRROR_STATUS_REJECTED,
            paymentMirrorStatusDetail = "ride_not_open"
        )
    }

    if (availableSeats <= 0) {
        return ApplyRideDecision.Apply(
            application = RideApplication(
                id = passengerId,
                rideId = rideId,
                passengerId = passengerId,
                passengerName = passengerName,
                passengerEmail = passengerEmail,
                status = APPLICATION_STATUS_REJECTED,
                paymentMethod = paymentOption,
                paymentStatus = PAYMENT_STATUS_UNPAID,
                isPaymentLocked = false,
                paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
                statusDetail = "ride_is_full",
                appliedAt = appliedAt,
                updatedAt = appliedAt
            ),
            paymentMethodId = paymentOption,
            paymentStatus = PAYMENT_STATUS_UNPAID,
            paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
            paymentMirrorStatus = PAYMENT_MIRROR_STATUS_REJECTED,
            paymentMirrorStatusDetail = "ride_is_full"
        )
    }

    val applicationPaymentMethod = if (paymentOption == PAYMENT_METHOD_BANK_TRANSFER) {
        PAYMENT_METHOD_BANK_TRANSFER
    } else {
        PAYMENT_METHOD_PENDING_SELECTION
    }

    val statusDetail = if (paymentOption == PAYMENT_METHOD_BANK_TRANSFER) {
        "bank_transfer_selected"
    } else {
        "application_created"
    }

    return ApplyRideDecision.Apply(
        application = RideApplication(
            id = passengerId,
            rideId = rideId,
            passengerId = passengerId,
            passengerName = passengerName,
            passengerEmail = passengerEmail,
            status = APPLICATION_STATUS_APPLIED,
            paymentMethod = applicationPaymentMethod,
            paymentStatus = PAYMENT_STATUS_PENDING,
            isPaymentLocked = false,
            paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
            statusDetail = statusDetail,
            appliedAt = appliedAt,
            updatedAt = appliedAt
        ),
        paymentMethodId = applicationPaymentMethod,
        paymentStatus = PAYMENT_STATUS_PENDING,
        paymentStatusSource = PAYMENT_STATUS_SOURCE_APPLICATION_CREATED,
        paymentMirrorStatus = PAYMENT_MIRROR_STATUS_PENDING,
        paymentMirrorStatusDetail = statusDetail
    )
}

internal fun buildRideCreationData(request: PublishRideRequest): Map<String, Any?> {
    return linkedMapOf(
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
        "seats" to request.totalSeats,
        "availableSeats" to request.totalSeats,
        "pricePerSeat" to request.pricePerSeat,
        "price" to request.pricePerSeat,
        "paymentOption" to request.paymentOption.trim().ifBlank { "card" },
        "passengerIds" to request.passengerIds,
        "driverRating" to request.driverRating,
        "onTimeRate" to request.onTimeRate,
        "reviewCount" to request.reviewCount,
        "verifiedByUniversity" to request.verifiedByUniversity,
        "carModel" to request.carModel,
        "licensePlate" to request.licensePlate,
        "notes" to request.notes,
        "publishedFromDraft" to request.publishedFromDraft,
        "sourceDraftId" to request.sourceDraftId,
        "status" to RIDE_STATUS_OPEN,
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun mapRideDocument(
    rideId: String,
    data: Map<String, Any?>
): Ride? {
    val departureTime = readInstant(data, "departureAt")
        ?: readInstant(data, "scheduledStartAt")
        ?: return null

    val destination = readString(data, "destination").orEmpty()
    val totalSeats = readInt(data, "totalSeats")
        ?: readInt(data, "seats")
        ?: 0
    val availableSeats = readInt(data, "availableSeats") ?: totalSeats
    val estimatedDurationMinutes = readInt(data, "estimatedDurationMinutes") ?: 30
    val driverRating = readDouble(data, "driverRating") ?: 5.0
    val reviewCount = readInt(data, "reviewCount") ?: 0
    val punctualityRate = readInt(data, "onTimeRate") ?: 100
    val pricePerSeat = readDouble(data, "pricePerSeat")
        ?: readDouble(data, "price")
        ?: readInt(data, "pricePerSeat")?.toDouble()
        ?: readInt(data, "price")?.toDouble()
        ?: 0.0

    return Ride(
        id = rideId,
        driverId = readString(data, "driverId").orEmpty(),
        driverName = readString(data, "driverName").orEmpty(),
        driverEmail = readString(data, "driverEmail").orEmpty(),
        driverRating = driverRating,
        reviewCount = reviewCount,
        reliabilityScore = 100,
        status = normalizeRideReadStatus(readString(data, "status")),
        origin = readString(data, "origin").orEmpty(),
        originCoordinates = readCoordinates(data, "originCoordinates"),
        destination = destination,
        destinationCoordinates = readCoordinates(data, "destinationCoordinates"),
        destinationArea = destination.substringAfterLast(",").trim().ifBlank { destination },
        departureTime = departureTime,
        estimatedDurationMinutes = estimatedDurationMinutes,
        availableSeats = availableSeats,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        punctualityRate = punctualityRate,
        isHabitRide = false,
        carModel = readString(data, "carModel").orEmpty(),
        licensePlate = readString(data, "licensePlate").orEmpty(),
        notes = readString(data, "notes") ?: readString(data, "description").orEmpty(),
        verifiedByUniversity = readBoolean(data, "verifiedByUniversity") ?: false,
        paymentOption = readString(data, "paymentOption").orEmpty().ifBlank { "card" },
        passengerIds = readStringList(data, "passengerIds")
    )
}

internal fun mapDriverRideDocument(
    rideId: String,
    data: Map<String, Any?>
): DriverRideRecord? {
    val departureAt = readInstant(data, "departureAt")
        ?: readInstant(data, "scheduledStartAt")
        ?: return null
    val estimatedDurationMinutes = readInt(data, "estimatedDurationMinutes")
        ?: readInstant(data, "estimatedArrivalAt")
            ?.let { arrival -> ((arrival.epochSecond - departureAt.epochSecond) / 60).toInt().coerceAtLeast(0) }
        ?: 30
    val destination = readString(data, "destination").orEmpty()

    return DriverRideRecord(
        id = rideId,
        driverId = readString(data, "driverId").orEmpty(),
        origin = readString(data, "origin").orEmpty(),
        originCoordinates = readCoordinates(data, "originCoordinates"),
        destination = destination,
        destinationCoordinates = readCoordinates(data, "destinationCoordinates"),
        departureAt = departureAt,
        estimatedDurationMinutes = estimatedDurationMinutes,
        availableSeats = readInt(data, "availableSeats") ?: 0,
        totalSeats = readInt(data, "totalSeats") ?: readInt(data, "seats") ?: 0,
        pricePerSeat = readInt(data, "pricePerSeat") ?: readInt(data, "price") ?: 0,
        carModel = readString(data, "carModel").orEmpty(),
        licensePlate = readString(data, "licensePlate").orEmpty(),
        notes = readString(data, "notes") ?: readString(data, "description").orEmpty(),
        driverName = readString(data, "driverName").orEmpty(),
        driverEmail = readString(data, "driverEmail").orEmpty(),
        status = normalizeRideReadStatus(readString(data, "status")),
        paymentOption = readString(data, "paymentOption").orEmpty().ifBlank { "card" }
    )
}

internal fun mapRideApplicationDocument(
    applicationId: String,
    data: Map<String, Any?>
): RideApplication? {
    val rideId = readString(data, "rideId").orEmpty()
    val passengerId = readString(data, "passengerId").orEmpty()
    if (rideId.isBlank() || passengerId.isBlank()) {
        return null
    }

    return RideApplication(
        id = applicationId,
        rideId = rideId,
        passengerId = passengerId,
        passengerName = readString(data, "passengerName").orEmpty(),
        passengerEmail = readString(data, "passengerEmail").orEmpty(),
        status = readString(data, "status").orEmpty().ifBlank { APPLICATION_STATUS_APPLIED },
        paymentMethod = readString(data, "paymentMethod").orEmpty().ifBlank { PAYMENT_METHOD_PENDING_SELECTION },
        paymentStatus = readString(data, "paymentStatus").orEmpty().ifBlank { PAYMENT_STATUS_PENDING },
        isPaymentLocked = readBoolean(data, "isPaymentLocked") ?: false,
        paymentStatusSource = readString(data, "paymentStatusSource").orEmpty().ifBlank { PAYMENT_STATUS_SOURCE_APPLICATION_CREATED },
        statusDetail = readString(data, "statusDetail"),
        appliedAt = readInstant(data, "appliedAt"),
        updatedAt = readInstant(data, "updatedAt")
    )
}

internal fun buildPassengerPaymentMirrorData(
    rideId: String,
    passengerId: String,
    paymentMethodId: String,
    paymentStatus: String,
    paymentStatusSource: String,
    isPaymentLocked: Boolean,
    status: String,
    statusDetail: String?
): Map<String, Any?> {
    return linkedMapOf(
        "rideId" to rideId,
        "passengerId" to passengerId,
        "paymentMethodId" to paymentMethodId,
        "paymentStatus" to paymentStatus,
        "paymentStatusSource" to paymentStatusSource,
        "isPaymentLocked" to isPaymentLocked,
        "status" to status,
        "statusDetail" to statusDetail,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun buildApplicationDocumentData(
    application: RideApplication
): Map<String, Any?> {
    return linkedMapOf(
        "id" to application.id,
        "rideId" to application.rideId,
        "passengerId" to application.passengerId,
        "passengerName" to application.passengerName,
        "passengerEmail" to application.passengerEmail,
        "status" to application.status,
        "paymentMethod" to application.paymentMethod,
        "paymentStatus" to application.paymentStatus,
        "isPaymentLocked" to application.isPaymentLocked,
        "paymentStatusSource" to application.paymentStatusSource,
        "statusDetail" to application.statusDetail,
        "appliedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun buildRideStatusUpdate(
    status: String
): Map<String, Any?> {
    return mapOf(
        "status" to normalizeRideWriteStatus(status),
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun buildRideFinishUpdate(): Map<String, Any?> {
    return mapOf(
        "status" to RIDE_STATUS_COMPLETED,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun buildRideCancellationUpdate(): Map<String, Any?> {
    return mapOf(
        "status" to RIDE_STATUS_CANCELLED,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}

internal fun readString(data: Map<String, Any?>, field: String): String? {
    return data[field] as? String
}

internal fun readInt(data: Map<String, Any?>, field: String): Int? {
    return when (val value = data[field]) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is Number -> value.toInt()
        else -> null
    }
}

internal fun readDouble(data: Map<String, Any?>, field: String): Double? {
    return when (val value = data[field]) {
        is Double -> value
        is Float -> value.toDouble()
        is Int -> value.toDouble()
        is Long -> value.toDouble()
        is Number -> value.toDouble()
        else -> null
    }
}

internal fun readBoolean(data: Map<String, Any?>, field: String): Boolean? {
    return data[field] as? Boolean
}

internal fun readStringList(data: Map<String, Any?>, field: String): List<String> {
    return when (val value = data[field]) {
        is List<*> -> value.mapNotNull { it as? String }.filter { it.isNotBlank() }
        else -> emptyList()
    }
}

internal fun readInstant(data: Map<String, Any?>, field: String): Instant? {
    return when (val value = data[field]) {
        is Timestamp -> value.toDate().toInstant()
        is Instant -> value
        else -> null
    }
}

internal fun readCoordinates(data: Map<String, Any?>, field: String): Coordinates? {
    return when (val value = data[field]) {
        is GeoPoint -> Coordinates(lat = value.latitude, lng = value.longitude)
        is Map<*, *> -> {
            val latitude = (value["lat"] as? Number)?.toDouble()
                ?: (value["latitude"] as? Number)?.toDouble()
            val longitude = (value["lng"] as? Number)?.toDouble()
                ?: (value["longitude"] as? Number)?.toDouble()
            if (latitude != null && longitude != null) {
                Coordinates(lat = latitude, lng = longitude)
            } else {
                null
            }
        }
        else -> null
    }
}

internal fun Coordinates.toGeoPoint(): GeoPoint {
    return GeoPoint(lat, lng)
}
