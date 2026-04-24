package com.wheels.app.features.rides.data.local

import android.util.LruCache
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.Ride
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearRidesLocalCache @Inject constructor() {
    private val memoryCache = LruCache<NearRidesCacheKey, CachedNearRides>(MAX_ENTRIES)

    @Synchronized
    fun get(query: NearRidesQuery): CachedNearRides? {
        return memoryCache.get(NearRidesCacheKey.from(query))
    }

    @Synchronized
    fun put(query: NearRidesQuery, rides: List<Ride>) {
        memoryCache.put(
            NearRidesCacheKey.from(query),
            CachedNearRides(
                rides = rides,
                cachedAtMillis = System.currentTimeMillis()
            )
        )
    }

    companion object {
        private const val MAX_ENTRIES = 32
    }
}
