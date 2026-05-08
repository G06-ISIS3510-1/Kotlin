package com.wheels.app.core.analytics.data.local

import com.wheels.app.core.analytics.domain.model.UserDestinationInsights
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDestinationInsightsLocalDataSource @Inject constructor(
    private val dao: UserDestinationInsightsDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getCached(userId: String): UserDestinationInsights? = withContext(ioDispatcher) {
        dao.getUserInsights(userId)?.toDomain()
    }

    suspend fun saveSnapshot(snapshot: UserDestinationInsights) = withContext(ioDispatcher) {
        dao.upsertInsights(snapshot.toEntity())
    }

    suspend fun clear(userId: String) = withContext(ioDispatcher) {
        dao.deleteInsights(userId)
    }
}
