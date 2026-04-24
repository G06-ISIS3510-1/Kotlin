package com.wheels.app.core.analytics.bq13.data.connectivity

import com.wheels.app.core.analytics.bq13.domain.connectivity.ConnectivityChecker
import com.wheels.app.core.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidConnectivityChecker @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : ConnectivityChecker {
    override fun isConnected(): Boolean = networkMonitor.isOnline()
}
