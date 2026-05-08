package com.wheels.app.features.rides.presentation.navigation

import android.content.Context
import com.wheels.app.core.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NavigationRetryManager(
    private val networkMonitor: NetworkMonitor
) {

    suspend fun retryLastIfPending(context: Context, navigationLauncher: NavigationLauncherService): Boolean {
        if (!networkMonitor.isOnline()) return false

        val store = NavigationStateStore(context)
        val pending = store.getPendingIntent() ?: return false

        return withContext(Dispatchers.Main) {
            val result = navigationLauncher.openDrivingDirections(
                context = context,
                destination = pending.destination,
                destinationCoordinates = pending.coordinates,
                origin = pending.origin,
                providerName = pending.provider
            )

            when (result) {
                is NavigationLauncherService.NavigationResult.Launched -> {
                    store.clear()
                    true
                }
                else -> false
            }
        }
    }
}
