package com.wheels.app.core.analytics.bq13.data.repository

import com.wheels.app.core.analytics.bq13.data.local.BQ13LocalDataSource
import com.wheels.app.core.analytics.bq13.data.remote.BQ13RemoteDataSource
import com.wheels.app.core.analytics.bq13.domain.connectivity.ConnectivityChecker
import com.wheels.app.core.analytics.bq13.domain.model.BQ13Result
import com.wheels.app.core.analytics.bq13.domain.repository.BQ13Repository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class BQ13RepositoryImpl @Inject constructor(
    private val remoteDataSource: BQ13RemoteDataSource,
    private val localDataSource: BQ13LocalDataSource,
    private val connectivityChecker: ConnectivityChecker,
    private val ioDispatcher: CoroutineDispatcher
) : BQ13Repository {

    override suspend fun getFrequentDestinations(userId: String): BQ13Result? {
        return withContext(ioDispatcher) {
            if (connectivityChecker.isConnected()) {
                val freshResult = remoteDataSource.fetchFrequentDestinations(userId)
                localDataSource.saveResult(freshResult)
                freshResult
            } else {
                localDataSource.getCachedResult(userId)
            }
        }
    }
}
