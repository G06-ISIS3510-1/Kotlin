package com.wheels.app.features.favoriteDrivers.data.local

import android.util.LruCache
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteDriverCache @Inject constructor() {
    private val cache = LruCache<String, FavoriteDriver>(MAX_ENTRIES)

    fun get(driverId: String): FavoriteDriver? {
        return synchronized(cache) { cache.get(driverId) }
    }

    fun put(driver: FavoriteDriver) {
        synchronized(cache) { cache.put(driver.driverId, driver) }
    }

    fun remove(driverId: String) {
        synchronized(cache) { cache.remove(driverId) }
    }

    fun putAll(drivers: List<FavoriteDriver>) {
        synchronized(cache) {
            drivers.forEach { cache.put(it.driverId, it) }
        }
    }

    companion object {
        private const val MAX_ENTRIES = 30
    }
}
