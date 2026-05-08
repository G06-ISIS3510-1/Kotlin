package com.wheels.app.core.analytics.data.repository

import com.wheels.app.core.analytics.data.local.UserDestinationInsightsLocalDataSource
import com.wheels.app.core.analytics.domain.model.UserDestinationInsights
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.core.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedUserDestinationInsightsRepository @Inject constructor(
    private val remote: com.wheels.app.core.analytics.data.repository.FirebaseUserDestinationInsightsRepository,
    private val local: UserDestinationInsightsLocalDataSource,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher
) : UserDestinationInsightsRepository {

    override suspend fun logRideBookedDestination(userId: String, rideId: String, destinationName: String) {
        // Always attempt remote when online; if offline, still write to remote will fail — keep behavior as remote-only
        if (!networkMonitor.isOnline()) {
            // If offline, just return (BQ13 is expected to be cached from backend; we do not queue these events)
            return
        }

        remote.logRideBookedDestination(userId, rideId, destinationName)
    }

    override fun observeUserDestinationInsights(userId: String): Flow<UserDestinationInsights?> =
        flow {
            val cached = withContext(ioDispatcher) { local.getCached(userId) }
            if (cached != null) emit(cached)

            if (!networkMonitor.isOnline()) {
                // offline: return cached (already emitted)
                return@flow
            }

            // If online, delegate to remote and save snapshot locally when available
            emitAll(
                remote.observeUserDestinationInsights(userId)
                    .map { value ->
                        value?.let {
                            if (it.lastUpdatedMillis != null) it else it.copy(lastUpdatedMillis = System.currentTimeMillis())
                        }
                    }
                    .onEach { value ->
                        if (value != null) {
                            withContext(ioDispatcher) { local.saveSnapshot(value) }
                        }
                    }
                    .catch { /* ignore remote errors, keep showing cache */ }
            )
        }

}
