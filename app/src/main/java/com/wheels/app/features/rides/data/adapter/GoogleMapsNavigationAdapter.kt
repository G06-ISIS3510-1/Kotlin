package com.wheels.app.features.rides.data.adapter

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import com.wheels.app.features.rides.domain.model.RouteDestination
import com.wheels.app.features.rides.domain.service.NavigationService

class GoogleMapsNavigationAdapter : NavigationService {

    override fun openRoute(
        context: Context,
        destination: RouteDestination,
        origin: String?
    ): Boolean {
        val packageManager = context.packageManager
        if (isPackageInstalled(packageManager, GOOGLE_MAPS_PACKAGE)) {
            val appIntent = Intent(
                Intent.ACTION_VIEW,
                buildNativeNavigationUri(destination = destination)
            ).apply {
                setPackage(GOOGLE_MAPS_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (launchIntent(context, appIntent)) {
                return true
            }
        }

        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            buildDirectionsUri(destination = destination, origin = origin)
        ).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (launchIntent(context, fallbackIntent)) {
            return true
        }

        return false
    }

    override fun isAvailable(context: Context): Boolean {
        return isPackageInstalled(context.packageManager, GOOGLE_MAPS_PACKAGE)
    }

    private fun buildNativeNavigationUri(destination: RouteDestination): Uri {
        val query = destination.coordinates?.let { "${it.lat},${it.lng}" }
            ?: buildQuery(destination)
            ?: DEFAULT_QUERY

        return Uri.parse("google.navigation:q=${Uri.encode(query)}&mode=d")
    }

    private fun buildDirectionsUri(destination: RouteDestination, origin: String?): Uri {
        val destinationQuery = buildQuery(destination)?.trim().orEmpty()
        val destinationValue = if (destinationQuery.isNotBlank()) {
            destinationQuery
        } else {
            DEFAULT_QUERY
        }

        return Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .path("/maps/dir/")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("destination", destinationValue)
            .appendQueryParameter("travelmode", "driving")
            .appendQueryParameter("utm_source", "Wheels")
            .appendQueryParameter("utm_campaign", "ride_navigation")
            .apply {
                origin?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { appendQueryParameter("origin", it) }
            }
            .build()
    }

    private fun buildQuery(destination: RouteDestination): String? {
        return when {
            !destination.address.isNullOrBlank() && !destination.name.isNullOrBlank() ->
                "${destination.name}, ${destination.address}"
            !destination.address.isNullOrBlank() -> destination.address
            !destination.name.isNullOrBlank() -> destination.name
            else -> null
        }
    }

    private fun launchIntent(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return runCatching {
            packageManager.getPackageInfo(packageName, 0)
        }.isSuccess
    }

    private companion object {
        const val DEFAULT_QUERY = "destination"
        const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }
}
