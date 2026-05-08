package com.wheels.app.features.rides.presentation.navigation

import android.content.Context
import android.widget.Toast
import com.wheels.app.core.network.AndroidNetworkMonitor
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.data.adapter.GoogleMapsNavigationAdapter
import com.wheels.app.features.rides.data.adapter.WazeNavigationAdapter
import com.wheels.app.features.rides.domain.model.RouteDestination
import com.wheels.app.features.rides.domain.service.NavigationService

class NavigationLauncherService {

    sealed class NavigationResult {
        object Launched : NavigationResult()
        object BlockedOffline : NavigationResult()
        data class Error(val message: String) : NavigationResult()
    }

    fun openDrivingDirections(
        context: Context,
        destination: String,
        destinationCoordinates: Coordinates? = null,
        origin: String? = null,
        providerName: String = GOOGLE_MAPS_PROVIDER
    ): NavigationResult {
        val cleanedDestination = destination.trim()
        if (cleanedDestination.isEmpty()) {
            return NavigationResult.Error("Invalid destination")
        }
        // Persist the intent so the user can retry if navigation fails or connectivity is lost.
        try {
            NavigationStateStore(context).saveIntent(
                destination = cleanedDestination,
                coordinates = destinationCoordinates,
                origin = origin,
                provider = providerName
            )
        } catch (_: Throwable) {
            // best-effort; ignore failures saving state
        }

        // If there's no connectivity, block the action and inform the caller.
        val networkMonitor = AndroidNetworkMonitor(context)
        if (!networkMonitor.isOnline()) {
            return NavigationResult.BlockedOffline
        }

        val launched = try {
            selectProvider(providerName).openRoute(
                context = context,
                destination = RouteDestination(
                    address = cleanedDestination,
                    coordinates = destinationCoordinates
                ),
                origin = origin?.trim()?.takeIf { it.isNotEmpty() }
            )
        } catch (t: Throwable) {
            false
        }

        if (launched) {
            try { NavigationStateStore(context).clear() } catch (_: Throwable) { }
            return NavigationResult.Launched
        }

        return NavigationResult.Error("Unable to open navigation provider.")
    }

    fun selectProvider(providerName: String): NavigationService {
        return when (providerName.lowercase()) {
            GOOGLE_MAPS_PROVIDER -> GoogleMapsNavigationAdapter()
            WAZE_PROVIDER -> WazeNavigationAdapter()
            else -> GoogleMapsNavigationAdapter()
        }
    }

    private companion object {
        const val GOOGLE_MAPS_PROVIDER = "google_maps"
        const val WAZE_PROVIDER = "waze"
    }
}
