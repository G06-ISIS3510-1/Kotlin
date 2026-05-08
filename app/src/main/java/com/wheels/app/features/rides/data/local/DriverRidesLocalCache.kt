package com.wheels.app.features.rides.data.local

import android.util.LruCache
import com.wheels.app.features.rides.domain.model.DriverRideRecord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverRidesLocalCache @Inject constructor() {

    private val memoryCache = LruCache<String, CachedDriverRides>(MAX_ENTRIES)

    @Synchronized
    fun get(driverId: String): CachedDriverRides? {
        return memoryCache.get(driverId)
    }

    @Synchronized
    fun put(driverId: String, rides: List<DriverRideRecord>) {
        memoryCache.put(
            driverId,
            CachedDriverRides(
                rides = rides,
                cachedAtMillis = System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val MAX_ENTRIES = 16
    }
}

data class CachedDriverRides(
    val rides: List<DriverRideRecord>,
    val cachedAtMillis: Long
)
