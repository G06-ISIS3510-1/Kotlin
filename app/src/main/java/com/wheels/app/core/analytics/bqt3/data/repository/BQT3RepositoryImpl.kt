package com.wheels.app.core.analytics.bqt3.data.repository

import com.wheels.app.core.analytics.bqt3.data.local.BQT3LocalDataSource
import com.wheels.app.core.analytics.bqt3.data.remote.BQT3RemoteDataSource
import com.wheels.app.core.analytics.bqt3.domain.connectivity.ConnectivityChecker
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3_FEATURE_NAME
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3UsageEvent
import com.wheels.app.core.analytics.bqt3.domain.model.BQT3WeeklyUsage
import com.wheels.app.core.analytics.bqt3.domain.repository.BQT3Repository
import com.wheels.app.core.network.NetworkMonitor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class BQT3RepositoryImpl @Inject constructor(
    private val remoteDataSource: BQT3RemoteDataSource,
    private val localDataSource: BQT3LocalDataSource,
    private val connectivityChecker: ConnectivityChecker,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher
) : BQT3Repository {

    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())

    init {
        repositoryScope.launch {
            var wasOnline = false
            networkMonitor.observeIsOnline().collect { isOnline ->
                if (isOnline && !wasOnline) {
                    // Transición de offline a online: sincronizar eventos pendientes
                    syncPendingEvents()
                }
                wasOnline = isOnline
            }
        }
    }

    override suspend fun trackRidesNearMeUsage(userId: String): BQT3WeeklyUsage {
        return withContext(ioDispatcher) {
            val event = BQT3UsageEvent(
                eventId = "bqt3_${userId}_${System.currentTimeMillis()}_${UUID.randomUUID()}",
                userId = userId,
                timestampMillis = System.currentTimeMillis(),
                weekStartDate = currentWeekStartDate(),
                featureName = BQT3_FEATURE_NAME,
                synced = false
            )

            localDataSource.saveEvent(event)

            if (connectivityChecker.isConnected()) {
                runCatching {
                    remoteDataSource.uploadEvent(event)
                    localDataSource.markEventsSynced(listOf(event.eventId))
                    syncPendingEvents()
                    refreshWeeklyUsage(userId, event.weekStartDate)
                }.getOrElse {
                    localDataSource.calculateOfflineWeeklyUsage(userId, event.weekStartDate)
                }
            } else {
                localDataSource.calculateOfflineWeeklyUsage(userId, event.weekStartDate)
            }
        }
    }

    override suspend fun getCurrentWeekUsage(userId: String): BQT3WeeklyUsage {
        return withContext(ioDispatcher) {
            val weekStartDate = currentWeekStartDate()
            if (connectivityChecker.isConnected()) {
                runCatching {
                    syncPendingEvents()
                    refreshWeeklyUsage(userId, weekStartDate)
                }.getOrElse {
                    localDataSource.calculateOfflineWeeklyUsage(userId, weekStartDate)
                }
            } else {
                localDataSource.calculateOfflineWeeklyUsage(userId, weekStartDate)
            }
        }
    }

    override suspend fun syncPendingEvents() {
        withContext(ioDispatcher) {
            if (!connectivityChecker.isConnected()) return@withContext

            val pendingEvents = localDataSource.getPendingEvents()
            val syncedEventIds = mutableListOf<String>()
            pendingEvents.forEach { event ->
                runCatching {
                    remoteDataSource.uploadEvent(event)
                }.onSuccess {
                    syncedEventIds += event.eventId
                }
            }

            localDataSource.markEventsSynced(syncedEventIds)
        }
    }

    private suspend fun refreshWeeklyUsage(
        userId: String,
        weekStartDate: String
    ): BQT3WeeklyUsage {
        val freshSummary = remoteDataSource.fetchWeeklyUsage(userId, weekStartDate)
        localDataSource.saveWeeklyUsage(freshSummary)
        return freshSummary
    }

    private fun currentWeekStartDate(): String {
        val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        return today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .toString()
    }
}
