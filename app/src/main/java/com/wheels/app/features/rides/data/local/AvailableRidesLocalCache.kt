package com.wheels.app.features.rides.data.local

import android.util.LruCache
import com.wheels.app.features.rides.domain.model.Ride
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailableRidesLocalCache @Inject constructor() {

    private val memoryCache = LruCache<String, CachedAvailableRides>(MAX_ENTRIES)

    @Synchronized
    fun get(): CachedAvailableRides? {
        return memoryCache.get(AVAILABLE_RIDES_CACHE_KEY)
    }

    @Synchronized
    fun put(rides: List<Ride>) {
        memoryCache.put(
            AVAILABLE_RIDES_CACHE_KEY,
            CachedAvailableRides(
                rides = rides,
                cachedAtMillis = System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val AVAILABLE_RIDES_CACHE_KEY = "available_rides"
        private const val MAX_ENTRIES = 1
    }
}

data class CachedAvailableRides(
    val rides: List<Ride>,
    val cachedAtMillis: Long
)
