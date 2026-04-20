package com.wheels.app.features.rides.presentation.navigation

import android.content.Context
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.data.adapter.GoogleMapsNavigationAdapter
import com.wheels.app.features.rides.data.adapter.WazeNavigationAdapter
import com.wheels.app.features.rides.domain.model.RouteDestination
import com.wheels.app.features.rides.domain.service.NavigationService

class NavigationLauncherService {

    fun openDrivingDirections(
        context: Context,
        destination: String,
        destinationCoordinates: Coordinates? = null,
        origin: String? = null,
        providerName: String = GOOGLE_MAPS_PROVIDER
    ): Boolean {
        val cleanedDestination = destination.trim()
        if (cleanedDestination.isEmpty()) {
            return false
        }

        return selectProvider(providerName).openRoute(
            context = context,
            destination = RouteDestination(
                address = cleanedDestination,
                coordinates = destinationCoordinates
            ),
            origin = origin?.trim()?.takeIf { it.isNotEmpty() }
        )
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
