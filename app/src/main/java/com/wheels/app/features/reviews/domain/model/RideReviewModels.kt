package com.wheels.app.features.reviews.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RideReview(
    val reviewId: String,
    val rideId: String,
    val driverId: String,
    val driverName: String,
    val passengerId: String,
    val passengerName: String,
    val stars: Int,
    val comment: String,
    val createdAt: Instant? = null
) {
    val hasRating: Boolean
        get() = stars in 1..5
}

data class DriverReviewSummary(
    val driverId: String,
    val reviewCount: Int,
    val ratedReviewCount: Int,
    val averageRating: Double,
    val starBreakdown: Map<Int, Int>
) {
    val displayRating: Double
        get() = if (ratedReviewCount > 0) averageRating else 0.0

    val displayRatingLabel: String
        get() = String.format(Locale.US, "%.1f", displayRating)

    val totalLabel: String
        get() = "$reviewCount review${if (reviewCount == 1) "" else "s"}"
}

data class SubmitRideReviewRequest(
    val rideId: String,
    val driverId: String,
    val driverName: String,
    val passengerId: String,
    val passengerName: String,
    val stars: Int,
    val comment: String
)

fun calculateDriverReviewSummary(reviews: List<RideReview>): Map<String, DriverReviewSummary> {
    return reviews
        .groupBy { it.driverId }
        .mapValues { entry ->
            val driverReviews = entry.value
            val ratedReviews = driverReviews.filter { it.hasRating }
            val breakdown = (5 downTo 1).associateWith { star ->
                ratedReviews.count { it.stars == star }
            }
            val averageRating = if (ratedReviews.isEmpty()) {
                0.0
            } else {
                ratedReviews.sumOf { it.stars }.toDouble() / ratedReviews.size.toDouble()
            }

            DriverReviewSummary(
                driverId = entry.key,
                reviewCount = driverReviews.size,
                ratedReviewCount = ratedReviews.size,
                averageRating = averageRating,
                starBreakdown = breakdown
            )
        }
}

fun RideReview.createdAtLabel(): String {
    val instant = createdAt ?: return "Just now"
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return formatter.format(instant.atZone(ZoneId.systemDefault()).toLocalDate())
}
