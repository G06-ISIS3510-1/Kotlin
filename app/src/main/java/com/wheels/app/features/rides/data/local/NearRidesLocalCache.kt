package com.wheels.app.features.rides.data.local

import android.util.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import com.wheels.app.features.rides.domain.model.NearRidesQuery
import com.wheels.app.features.rides.domain.model.Ride
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearRidesLocalCache @Inject constructor(
    private val rideOfflineDao: RideOfflineDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val memoryCache = LruCache<NearRidesCacheKey, CachedNearRides>(MAX_ENTRIES)

    suspend fun get(query: NearRidesQuery): CachedNearRides? {
        val cacheKey = NearRidesCacheKey.from(query)

        synchronized(memoryCache) {
            memoryCache.get(cacheKey)?.let { return it }
        }

        val persisted = withContext(ioDispatcher) {
            rideOfflineDao.getNearRidesCache(cacheKey.toStorageKey())
        }?.toCachedNearRides()

        if (persisted != null) {
            synchronized(memoryCache) {
                memoryCache.put(cacheKey, persisted)
            }
        }

        return persisted
    }

    suspend fun getLatest(): CachedNearRides? {
        val persisted = withContext(ioDispatcher) {
            rideOfflineDao.getLatestNearRidesCache()
        }?.toCachedNearRides()

        if (persisted != null) {
            val cacheKey = NearRidesCacheKey(
                latBucket = 0,
                lngBucket = 0,
                radiusMeters = 0,
                destinationQuery = ""
            )
            synchronized(memoryCache) {
                memoryCache.put(cacheKey, persisted)
            }
        }

        return persisted
    }

    suspend fun put(query: NearRidesQuery, rides: List<Ride>) {
        val cacheKey = NearRidesCacheKey.from(query)
        val cached = CachedNearRides(
            rides = rides,
            cachedAtMillis = System.currentTimeMillis()
        )

        synchronized(memoryCache) {
            memoryCache.put(cacheKey, cached)
        }

        withContext(ioDispatcher) {
            rideOfflineDao.upsertNearRidesCache(cached.toEntity(cacheKey))
        }
    }

    companion object {
        private const val MAX_ENTRIES = 32
    }
}
