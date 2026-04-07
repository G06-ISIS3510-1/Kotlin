package com.wheels.app.features.rides.data.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.wheels.app.features.rides.domain.model.RouteDestination
import com.wheels.app.features.rides.domain.service.NavigationService

class WazeNavigationAdapter : NavigationService {

    override fun openRoute(
        context: Context,
        destination: RouteDestination,
        origin: String?
    ): Boolean {
        val packageManager = context.packageManager
        val appUri = buildAppUri(destination)
        val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
            setPackage(WAZE_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (canHandleIntent(packageManager, appIntent)) {
            context.startActivity(appIntent)
            return true
        }

        val fallbackUri = buildWebUri(destination)
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (canHandleIntent(packageManager, fallbackIntent)) {
            context.startActivity(fallbackIntent)
            return true
        }

        return false
    }

    override fun isAvailable(context: Context): Boolean {
        return isPackageInstalled(context.packageManager, WAZE_PACKAGE)
    }

    private fun buildAppUri(destination: RouteDestination): Uri {
        val encodedQuery = buildQuery(destination)?.let(Uri::encode)
        val latLng = destination.coordinates?.let { "${it.lat},${it.lng}" }

        val uri = when {
            latLng != null -> "waze://?ll=$latLng&navigate=yes"
            encodedQuery != null -> "waze://?q=$encodedQuery&navigate=yes"
            else -> "waze://?q=${Uri.encode(DEFAULT_QUERY)}&navigate=yes"
        }

        return Uri.parse(uri)
    }

    private fun buildWebUri(destination: RouteDestination): Uri {
        val encodedQuery = buildQuery(destination)?.let(Uri::encode)
        val latLng = destination.coordinates?.let { "${it.lat},${it.lng}" }

        val uri = when {
            latLng != null -> "https://waze.com/ul?ll=$latLng&navigate=yes"
            encodedQuery != null -> "https://waze.com/ul?q=$encodedQuery&navigate=yes"
            else -> "https://waze.com/ul?q=${Uri.encode(DEFAULT_QUERY)}&navigate=yes"
        }

        return Uri.parse(uri)
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

    private fun canHandleIntent(packageManager: PackageManager, intent: Intent): Boolean {
        return intent.resolveActivity(packageManager) != null
    }

    private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return runCatching {
            packageManager.getPackageInfo(packageName, 0)
        }.isSuccess
    }

    private companion object {
        const val WAZE_PACKAGE = "com.waze"
        const val DEFAULT_QUERY = "destination"
    }
}
