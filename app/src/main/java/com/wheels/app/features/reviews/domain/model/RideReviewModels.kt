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

sealed interface DriverReviewsFeed {
    data class Cached(val reviews: List<RideReview>) : DriverReviewsFeed
    data class Fresh(val reviews: List<RideReview>) : DriverReviewsFeed
    data class RefreshError(val message: String) : DriverReviewsFeed
}

fun calculateDriverReviewSummary(reviews: List<RideReview>): Map<String, DriverReviewSummary> {
    data class DriverReviewAccumulator(
        var reviewCount: Int = 0,
        var ratedReviewCount: Int = 0,
        var ratingTotal: Int = 0,
        val starCounts: IntArray = IntArray(6)
    )

    val accumulators = linkedMapOf<String, DriverReviewAccumulator>()

    for (review in reviews) {
        val accumulator = accumulators.getOrPut(review.driverId) { DriverReviewAccumulator() }
        accumulator.reviewCount += 1

        if (review.hasRating) {
            accumulator.ratedReviewCount += 1
            accumulator.ratingTotal += review.stars
            accumulator.starCounts[review.stars] += 1
        }
    }

    return accumulators.mapValues { (driverId, accumulator) ->
        DriverReviewSummary(
            driverId = driverId,
            reviewCount = accumulator.reviewCount,
            ratedReviewCount = accumulator.ratedReviewCount,
            averageRating = if (accumulator.ratedReviewCount == 0) {
                0.0
            } else {
                accumulator.ratingTotal.toDouble() / accumulator.ratedReviewCount.toDouble()
            },
            starBreakdown = linkedMapOf(
                5 to accumulator.starCounts[5],
                4 to accumulator.starCounts[4],
                3 to accumulator.starCounts[3],
                2 to accumulator.starCounts[2],
                1 to accumulator.starCounts[1]
            )
        )
    }
}

fun upsertDriverReview(existingReviews: List<RideReview>, review: RideReview): List<RideReview> {
    return (existingReviews.filterNot { it.reviewId == review.reviewId } + review)
        .sortedByDescending { it.createdAt ?: Instant.EPOCH }
}

fun RideReview.createdAtLabel(): String {
    val instant = createdAt ?: return "Just now"
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return formatter.format(instant.atZone(ZoneId.systemDefault()).toLocalDate())
}
