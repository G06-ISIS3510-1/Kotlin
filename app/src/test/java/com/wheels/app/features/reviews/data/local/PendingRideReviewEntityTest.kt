package com.wheels.app.features.reviews.data.local

import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class PendingRideReviewEntityTest {

    @Test
    fun `pending review entity trims comment and keeps queue key stable`() {
        val request = SubmitRideReviewRequest(
            rideId = "ride-42",
            driverId = "driver-7",
            driverName = "Carlos Mendez",
            passengerId = "passenger-9",
            passengerName = "Ana",
            stars = 5,
            comment = "  Great ride  "
        )

        val pending = request.toPendingRideReviewEntity(passengerName = request.passengerName, createdAtMillis = 1_700_000_000_000)

        assertEquals("passenger-9_ride-42", pending.reviewKey)
        assertEquals("  Great ride  ".trim(), pending.comment)
        assertEquals(1_700_000_000_000, pending.createdAtMillis)
    }

    @Test
    fun `pending review entity converts back to ride review`() {
        val pending = PendingRideReviewEntity(
            reviewKey = "passenger-9_ride-42",
            rideId = "ride-42",
            driverId = "driver-7",
            driverName = "Carlos Mendez",
            passengerId = "passenger-9",
            passengerName = "Ana",
            stars = 4,
            comment = "Smooth trip",
            createdAtMillis = 1_700_000_000_000
        )

        val review = pending.toRideReview()

        assertEquals("driver-7_passenger-9", review.reviewId)
        assertEquals("ride-42", review.rideId)
        assertEquals("driver-7", review.driverId)
        assertEquals("Carlos Mendez", review.driverName)
        assertEquals("passenger-9", review.passengerId)
        assertEquals("Ana", review.passengerName)
        assertEquals(4, review.stars)
        assertEquals("Smooth trip", review.comment)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000), review.createdAt)
    }

    @Test
    fun `pending ride review key uses passenger id and ride id`() {
        assertEquals(
            "passenger-9_ride-42",
            buildPendingRideReviewKey("ride-42", "passenger-9")
        )
    }
}
