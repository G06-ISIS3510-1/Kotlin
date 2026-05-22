package com.wheels.app.features.reviews.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.calculateDriverReviewSummary
import com.wheels.app.features.reviews.domain.model.upsertDriverReview
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RideReviewRepositoryTest {

    @Test
    fun `buildReviewDocumentData writes expected review payload`() {
        val review = RideReview(
            reviewId = "ride-1_passenger-1",
            rideId = "ride-1",
            driverId = "driver-1",
            driverName = "Carlos Mendez",
            passengerId = "passenger-1",
            passengerName = "Ana",
            stars = 5,
            comment = "Great ride",
            createdAt = Instant.ofEpochSecond(1_700_000_000)
        )

        val data = buildReviewDocumentData(review)

        assertEquals("ride-1_passenger-1", data["reviewId"])
        assertEquals("driver-1", data["driverId"])
        assertEquals("Carlos Mendez", data["driverName"])
        assertEquals("passenger-1", data["passengerId"])
        assertEquals("Ana", data["passengerName"])
        assertEquals(5, data["stars"])
        assertEquals("Great ride", data["comment"])
        assertTrue(data["createdAt"] is FieldValue)
        assertTrue(data["updatedAt"] is FieldValue)
    }

    @Test
    fun `mapReviewDocument preserves review shape and timestamp`() {
        val createdAt = Timestamp(1_700_000_000, 0)
        val review = mapReviewDocument(
            reviewId = "ride-2_passenger-2",
            data = mapOf(
                "driverId" to "driver-2",
                "driverName" to "Maria Sanchez",
                "passengerId" to "passenger-2",
                "passengerName" to "Luis",
                "stars" to 4,
                "comment" to "Smooth trip",
                "createdAt" to createdAt
            )
        )

        assertNotNull(review)
        requireNotNull(review)
        assertEquals("ride-2_passenger-2", review.reviewId)
        assertEquals("", review.rideId)
        assertEquals("driver-2", review.driverId)
        assertEquals("Maria Sanchez", review.driverName)
        assertEquals("passenger-2", review.passengerId)
        assertEquals("Luis", review.passengerName)
        assertEquals(4, review.stars)
        assertEquals("Smooth trip", review.comment)
        assertEquals(createdAt.toDate().toInstant(), review.createdAt)
    }

    @Test
    fun `summary ignores unrated reviews and computes star breakdown`() {
        val summaries = calculateDriverReviewSummary(
            listOf(
                RideReview(
                    reviewId = "r1",
                    rideId = "ride-1",
                    driverId = "driver-1",
                    driverName = "Carlos",
                    passengerId = "passenger-1",
                    passengerName = "Ana",
                    stars = 5,
                    comment = "Awesome"
                ),
                RideReview(
                    reviewId = "r2",
                    rideId = "ride-2",
                    driverId = "driver-1",
                    driverName = "Carlos",
                    passengerId = "passenger-2",
                    passengerName = "Luis",
                    stars = 4,
                    comment = "Great"
                ),
                RideReview(
                    reviewId = "r3",
                    rideId = "ride-3",
                    driverId = "driver-1",
                    driverName = "Carlos",
                    passengerId = "passenger-3",
                    passengerName = "Maria",
                    stars = 0,
                    comment = "Comment only"
                )
            )
        )

        val summary = summaries["driver-1"]
        assertNotNull(summary)
        requireNotNull(summary)
        assertEquals(3, summary.reviewCount)
        assertEquals(2, summary.ratedReviewCount)
        assertEquals(4.5, summary.averageRating, 0.0001)
        assertEquals(1, summary.starBreakdown[5])
        assertEquals(1, summary.starBreakdown[4])
        assertEquals(0, summary.starBreakdown[3])
        assertEquals(0, summary.starBreakdown[2])
        assertEquals(0, summary.starBreakdown[1])
    }

    @Test
    fun `summary computes each driver's totals independently`() {
        val summaries = calculateDriverReviewSummary(
            listOf(
                RideReview(
                    reviewId = "r1",
                    rideId = "ride-1",
                    driverId = "driver-1",
                    driverName = "Carlos",
                    passengerId = "passenger-1",
                    passengerName = "Ana",
                    stars = 5,
                    comment = "Awesome"
                ),
                RideReview(
                    reviewId = "r2",
                    rideId = "ride-2",
                    driverId = "driver-2",
                    driverName = "Maria",
                    passengerId = "passenger-2",
                    passengerName = "Luis",
                    stars = 3,
                    comment = "Okay"
                ),
                RideReview(
                    reviewId = "r3",
                    rideId = "ride-3",
                    driverId = "driver-2",
                    driverName = "Maria",
                    passengerId = "passenger-3",
                    passengerName = "Sofia",
                    stars = 0,
                    comment = "Comment only"
                )
            )
        )

        val driverOne = summaries["driver-1"]
        val driverTwo = summaries["driver-2"]

        assertNotNull(driverOne)
        assertNotNull(driverTwo)
        requireNotNull(driverOne)
        requireNotNull(driverTwo)

        assertEquals(1, driverOne.reviewCount)
        assertEquals(1, driverOne.ratedReviewCount)
        assertEquals(5.0, driverOne.averageRating, 0.0001)
        assertEquals(1, driverOne.starBreakdown[5])

        assertEquals(2, driverTwo.reviewCount)
        assertEquals(1, driverTwo.ratedReviewCount)
        assertEquals(3.0, driverTwo.averageRating, 0.0001)
        assertEquals(1, driverTwo.starBreakdown[3])
        assertEquals(0, driverTwo.starBreakdown[5])
    }

    @Test
    fun `review id is derived from driver and passenger only`() {
        assertEquals(
            "driver-1_passenger-1",
            buildReviewId("driver-1", "passenger-1")
        )
    }

    @Test
    fun `upsertDriverReview replaces existing review and keeps newest first`() {
        val existing = listOf(
            RideReview(
                reviewId = "driver-1_passenger-1",
                rideId = "",
                driverId = "driver-1",
                driverName = "Carlos",
                passengerId = "passenger-1",
                passengerName = "Ana",
                stars = 4,
                comment = "Old review",
                createdAt = Instant.EPOCH
            ),
            RideReview(
                reviewId = "driver-1_passenger-2",
                rideId = "",
                driverId = "driver-1",
                driverName = "Carlos",
                passengerId = "passenger-2",
                passengerName = "Luis",
                stars = 5,
                comment = "Another review",
                createdAt = Instant.EPOCH.plusSeconds(10)
            )
        )
        val updated = RideReview(
            reviewId = "driver-1_passenger-1",
            rideId = "",
            driverId = "driver-1",
            driverName = "Carlos",
            passengerId = "passenger-1",
            passengerName = "Ana",
            stars = 5,
            comment = "Fresh review",
            createdAt = Instant.EPOCH.plusSeconds(20)
        )

        val merged = upsertDriverReview(existing, updated)

        assertEquals(2, merged.size)
        assertEquals("Fresh review", merged.first().comment)
        assertEquals("driver-1_passenger-2", merged[1].reviewId)
    }
}
