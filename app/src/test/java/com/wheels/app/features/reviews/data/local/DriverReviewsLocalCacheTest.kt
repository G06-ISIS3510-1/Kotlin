package com.wheels.app.features.reviews.data.local

import com.wheels.app.features.reviews.domain.model.RideReview
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DriverReviewsLocalCacheTest {

    @Test
    fun `get and put work for a driver key`() {
        val cache = DriverReviewsLocalCache(FixedReviewsCacheSizer(3))
        val reviews = listOf(sampleReview("driver-1", "passenger-1"))

        cache.put("driver-1", reviews)

        val cached = cache.get("driver-1")
        assertEquals(reviews, cached?.reviews)
        assertEquals(1, cached?.reviews?.size)
    }

    @Test
    fun `recently accessed entries are retained over older ones`() {
        val cache = DriverReviewsLocalCache(FixedReviewsCacheSizer(3))

        repeat(3) { index ->
            cache.put("driver-$index", listOf(sampleReview("driver-$index", "passenger-$index")))
        }

        cache.get("driver-0")
        cache.put("driver-new", listOf(sampleReview("driver-new", "passenger-new")))

        assertNull(cache.get("driver-1"))
        assertEquals("driver-0", cache.get("driver-0")?.reviews?.firstOrNull()?.driverId)
        assertEquals("driver-new", cache.get("driver-new")?.reviews?.firstOrNull()?.driverId)
    }

    @Test
    fun `least recently used entry is evicted when cache limit is exceeded`() {
        val cache = DriverReviewsLocalCache(FixedReviewsCacheSizer(3))

        repeat(4) { index ->
            cache.put("driver-$index", listOf(sampleReview("driver-$index", "passenger-$index")))
        }

        assertNull(cache.get("driver-0"))
        assertEquals("driver-1", cache.get("driver-1")?.reviews?.firstOrNull()?.driverId)
        assertEquals("driver-3", cache.get("driver-3")?.reviews?.firstOrNull()?.driverId)
    }

    @Test
    fun `cache size scales with memory class`() {
        assertEquals(8, calculateReviewsCacheMaxEntries(96, isLowRamDevice = false))
        assertEquals(12, calculateReviewsCacheMaxEntries(192, isLowRamDevice = false))
        assertEquals(6, calculateReviewsCacheMaxEntries(96, isLowRamDevice = true))
    }

    private fun sampleReview(driverId: String, passengerId: String): RideReview {
        return RideReview(
            reviewId = "${driverId}_${passengerId}",
            rideId = "",
            driverId = driverId,
            driverName = "Driver",
            passengerId = passengerId,
            passengerName = "Passenger",
            stars = 5,
            comment = "Great ride",
            createdAt = Instant.EPOCH
        )
    }

    private class FixedReviewsCacheSizer(
        private val entries: Int
    ) : ReviewsCacheSizer {
        override fun maxEntries(): Int = entries
    }
}
