package com.wheels.app.core.location.data.provider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.wheels.app.core.location.domain.model.CurrentLocationLabel
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.features.rides.domain.model.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FusedCurrentLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : CurrentLocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocationLabel(): CurrentLocationLabel = withContext(ioDispatcher) {
        val location = getCurrentLocation()
        reverseGeocode(location)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinates(): Coordinates = withContext(ioDispatcher) {
        val location = getCurrentLocation()
        Coordinates(
            lat = location.latitude,
            lng = location.longitude
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location {
        if (!hasLocationPermission()) {
            throw IllegalStateException("Location permission is required to use current location.")
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return client.lastLocation.awaitNullable()
            ?: client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).awaitNullable()
            ?: throw IllegalStateException("We could not determine your current location.")
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private suspend fun reverseGeocode(location: Location): CurrentLocationLabel {
        if (!Geocoder.isPresent()) {
            return fallbackLocationLabel(location)
        }

        val geocoder = Geocoder(context, Locale.getDefault())

        val address = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationSuspend(location.latitude, location.longitude)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()
            }
        }.getOrNull()

        return if (address != null) {
            val specificTitle = listOfNotNull(
                address.thoroughfare?.takeIf { it.isNotBlank() },
                address.subThoroughfare?.takeIf { it.isNotBlank() }
            ).joinToString(" ").ifBlank { null }

            val areaFallback = listOfNotNull(
                address.subLocality?.takeIf { it.isNotBlank() },
                address.locality?.takeIf { it.isNotBlank() }
            ).joinToString(", ").ifBlank { null }

            CurrentLocationLabel(
                title = specificTitle
                    ?: address.featureName
                    ?: address.getAddressLine(0)
                    ?: areaFallback
                    ?: fallbackLocationLabel(location).title,
                subtitle = listOfNotNull(
                    address.subLocality,
                    address.locality,
                    address.subAdminArea
                ).joinToString(", ").ifBlank { null }
            )
        } else {
            fallbackLocationLabel(location)
        }
    }

    private fun fallbackLocationLabel(location: Location): CurrentLocationLabel {
        return CurrentLocationLabel(
            title = "Current location",
            subtitle = "${"%.5f".format(location.latitude)}, ${"%.5f".format(location.longitude)}"
        )
    }

    private suspend fun Task<Location>.awaitNullable(): Location? {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            addOnCanceledListener { continuation.resume(null) }
        }
    }

    private suspend fun Geocoder.getFromLocationSuspend(
        latitude: Double,
        longitude: Double
    ): Address? {
        return suspendCancellableCoroutine { continuation ->
            try {
                getFromLocation(latitude, longitude, 1) { addresses ->
                    continuation.resume(addresses.firstOrNull())
                }
            } catch (exception: IOException) {
                continuation.resumeWithException(exception)
            } catch (exception: IllegalArgumentException) {
                continuation.resumeWithException(exception)
            }
        }
    }
}
