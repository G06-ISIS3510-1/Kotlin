package com.wheels.app.features.reviews.data.local

import androidx.collection.ArrayMap
import com.wheels.app.features.reviews.domain.model.RideReview
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverReviewsLocalCache @Inject constructor(
    private val reviewsCacheSizer: ReviewsCacheSizer
) {

    private val maxEntries: Int = reviewsCacheSizer.maxEntries().coerceAtLeast(1)
    private val memoryCache = ArrayMap<String, CachedDriverReviews>(maxEntries)
    private val accessOrder = ArrayDeque<String>()

    @Synchronized
    fun get(driverId: String): CachedDriverReviews? {
        val cached = memoryCache[driverId] ?: return null
        touch(driverId)
        return cached
    }

    @Synchronized
    fun put(driverId: String, reviews: List<RideReview>) {
        memoryCache[driverId] = CachedDriverReviews(
            reviews = reviews,
            cachedAtMillis = System.currentTimeMillis()
        )
        touch(driverId)
        trimToSize()
    }

    @Synchronized
    fun clear(driverId: String) {
        memoryCache.remove(driverId)
        accessOrder.remove(driverId)
    }

    @Synchronized
    fun clearAll() {
        memoryCache.clear()
        accessOrder.clear()
    }

    private fun touch(driverId: String) {
        accessOrder.remove(driverId)
        accessOrder.addLast(driverId)
    }

    private fun trimToSize() {
        while (memoryCache.size > maxEntries) {
            val oldestKey = accessOrder.removeFirstOrNull() ?: break
            memoryCache.remove(oldestKey)
        }
    }
}

data class CachedDriverReviews(
    val reviews: List<RideReview>,
    val cachedAtMillis: Long
)

interface ReviewsCacheSizer {
    fun maxEntries(): Int
}

internal fun calculateReviewsCacheMaxEntries(
    memoryClassMb: Int,
    isLowRamDevice: Boolean
): Int {
    val scaledEntries = (memoryClassMb / 16).coerceIn(8, 64)
    return if (isLowRamDevice) {
        (scaledEntries / 2).coerceAtLeast(6)
    } else {
        scaledEntries
    }
}
