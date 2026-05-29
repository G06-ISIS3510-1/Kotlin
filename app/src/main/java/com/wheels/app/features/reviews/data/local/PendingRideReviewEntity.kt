package com.wheels.app.features.reviews.data.local

import com.wheels.app.features.reviews.data.repository.buildReviewId
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import java.time.Instant

data class PendingRideReviewEntity(
    val reviewKey: String,
    val rideId: String,
    val driverId: String,
    val driverName: String,
    val passengerId: String,
    val passengerName: String,
    val stars: Int,
    val comment: String,
    val createdAtMillis: Long
)

fun SubmitRideReviewRequest.toPendingRideReviewEntity(
    passengerName: String,
    createdAtMillis: Long = System.currentTimeMillis()
): PendingRideReviewEntity {
    return PendingRideReviewEntity(
        reviewKey = buildPendingRideReviewKey(rideId, passengerId),
        rideId = rideId,
        driverId = driverId,
        driverName = driverName,
        passengerId = passengerId,
        passengerName = passengerName,
        stars = stars.coerceIn(0, 5),
        comment = comment.trim(),
        createdAtMillis = createdAtMillis
    )
}

fun PendingRideReviewEntity.toRideReview(): RideReview {
    return RideReview(
        reviewId = buildReviewId(driverId, passengerId),
        rideId = rideId,
        driverId = driverId,
        driverName = driverName,
        passengerId = passengerId,
        passengerName = passengerName,
        stars = stars.coerceIn(0, 5),
        comment = comment.trim(),
        createdAt = Instant.ofEpochMilli(createdAtMillis)
    )
}

fun buildPendingRideReviewKey(rideId: String, passengerId: String): String {
    return "${passengerId}_$rideId"
}
