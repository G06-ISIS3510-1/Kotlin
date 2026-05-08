package com.wheels.app.features.rides.presentation.navigation

import android.content.Context
import com.wheels.app.core.network.AndroidNetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NavigationAutoRetry(private val context: Context) {

    private var job: Job? = null

    fun start(navigationLauncher: NavigationLauncherService) {
        // Avoid multiple concurrent watchers
        if (job?.isActive == true) return

        val networkMonitor = AndroidNetworkMonitor(context)
        val retryManager = NavigationRetryManager(networkMonitor)

        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                // Wait until network becomes available
                networkMonitor.observeIsOnline()
                    .filter { isOnline -> isOnline }
                    .first()

                // Attempt retry once
                retryManager.retryLastIfPending(context, navigationLauncher)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
