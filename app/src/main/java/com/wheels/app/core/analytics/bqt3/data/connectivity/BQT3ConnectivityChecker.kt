package com.wheels.app.core.analytics.bqt3.data.connectivity

import com.wheels.app.core.analytics.bqt3.domain.connectivity.ConnectivityChecker
import com.wheels.app.core.network.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BQT3ConnectivityChecker @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : ConnectivityChecker {
    override fun isConnected(): Boolean = networkMonitor.isOnline()
}
