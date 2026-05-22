package com.wheels.app.features.favoriteDrivers.data.local

import com.wheels.app.features.favoriteDrivers.domain.model.FavoriteDriver
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoriteDriverLocalDataSource @Inject constructor(
    private val dao: FavoriteDriverDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun observeFavoriteDrivers(): Flow<List<FavoriteDriver>> {
        return dao.observeFavoriteDrivers().map { drivers -> drivers.map { it.toDomain() } }
    }

    fun observeFavoriteDriver(driverId: String): Flow<FavoriteDriver?> {
        return dao.observeFavoriteDriver(driverId).map { it?.toDomain() }
    }

    suspend fun getFavoriteDriver(driverId: String): FavoriteDriver? = withContext(ioDispatcher) {
        dao.getFavoriteDriver(driverId)?.toDomain()
    }

    suspend fun getFavoriteDriverEntity(driverId: String): FavoriteDriverEntity? = withContext(ioDispatcher) {
        dao.getFavoriteDriver(driverId)
    }

    suspend fun getPendingSyncDrivers(): List<FavoriteDriver> = withContext(ioDispatcher) {
        dao.getPendingSyncDrivers().map { it.toDomain() }
    }

    suspend fun saveFavoriteDriver(driver: FavoriteDriver) = withContext(ioDispatcher) {
        dao.upsertFavoriteDriver(driver.toEntity())
    }

    suspend fun markSynced(driverId: String) = withContext(ioDispatcher) {
        dao.markSynced(driverId)
    }

    suspend fun markPendingDelete(driverId: String) = withContext(ioDispatcher) {
        dao.markPendingDelete(driverId)
    }

    suspend fun deleteFavoriteDriver(driverId: String) = withContext(ioDispatcher) {
        dao.deleteFavoriteDriver(driverId)
    }
}
