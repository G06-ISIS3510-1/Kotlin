package com.wheels.app.features.favoriteDrivers.data.repository

import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.features.favoriteDrivers.analytics.domain.service.FavoriteDriverAnalyticsService
import com.wheels.app.features.favoriteDrivers.data.local.FavoriteDriverCache
import com.wheels.app.features.favoriteDrivers.data.local.FavoriteDriverLocalDataSource
import com.wheels.app.features.favoriteDrivers.data.local.toDomain
import com.wheels.app.features.favoriteDrivers.data.remote.FavoriteDriverRemoteDataSource
import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import com.wheels.app.features.favoriteDrivers.domain.repository.FavoriteDriverRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class FavoriteDriverRepositoryImpl @Inject constructor(
    private val localDataSource: FavoriteDriverLocalDataSource,
    private val remoteDataSource: FavoriteDriverRemoteDataSource,
    private val analyticsService: FavoriteDriverAnalyticsService,
    private val cache: FavoriteDriverCache,
    private val networkMonitor: NetworkMonitor
) : FavoriteDriverRepository {

    override fun observeFavoriteDrivers(): Flow<List<FavoriteDriver>> {
        return localDataSource.observeFavoriteDrivers().map { drivers ->
            cache.putAll(drivers)
            drivers
        }
    }

    override fun observeFavoriteDriver(driverId: String): Flow<FavoriteDriver?> {
        return localDataSource.observeFavoriteDriver(driverId).map { driver ->
            if (driver != null) {
                cache.put(driver)
                driver
            } else {
                cache.get(driverId)
            }
        }
    }

    override suspend fun favoriteDriver(driver: FavoriteDriver) {
        val pendingDriver = driver.copy(
            savedAt = System.currentTimeMillis(),
            pendingSync = true
        )
        localDataSource.saveFavoriteDriver(pendingDriver)
        cache.put(pendingDriver)
        analyticsService.trackFavoriteAdded(pendingDriver)
    }

    override suspend fun unfavoriteDriver(driverId: String) {
        val existingDriver = localDataSource.getFavoriteDriverEntity(driverId)
        if (existingDriver == null) {
            localDataSource.deleteFavoriteDriver(driverId)
            cache.remove(driverId)
            return
        }

        localDataSource.markPendingDelete(driverId)
        cache.remove(driverId)
        analyticsService.trackFavoriteRemoved(existingDriver.toDomain())
    }

    override suspend fun syncPendingFavorites() {
        if (!networkMonitor.isOnline()) return

        localDataSource.getPendingSyncDrivers().forEach { driver ->
            val entity = localDataSource.getFavoriteDriverEntity(driver.driverId)
            runCatching {
                if (entity?.pendingDelete == true) {
                    remoteDataSource.deleteFavorite(driver.driverId)
                } else {
                    remoteDataSource.syncFavorite(driver)
                }
            }.onSuccess {
                if (entity?.pendingDelete == true) {
                    localDataSource.deleteFavoriteDriver(driver.driverId)
                    cache.remove(driver.driverId)
                } else {
                    localDataSource.markSynced(driver.driverId)
                    cache.put(driver.copy(pendingSync = false))
                }
            }
        }
    }
}
