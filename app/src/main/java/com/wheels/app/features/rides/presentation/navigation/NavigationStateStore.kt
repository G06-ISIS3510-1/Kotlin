package com.wheels.app.features.rides.presentation.navigation

import android.content.Context
import com.wheels.app.features.rides.domain.model.Coordinates

class NavigationStateStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveIntent(destination: String, coordinates: Coordinates?, origin: String?, provider: String) {
        prefs.edit().apply {
            putString(KEY_DESTINATION, destination)
            putString(KEY_ORIGIN, origin)
            putString(KEY_PROVIDER, provider)
            coordinates?.let { putString(KEY_LAT, it.lat.toString()); putString(KEY_LNG, it.lng.toString()) }
        }.apply()
    }

    fun getPendingIntent(): PendingNavigationIntent? {
        val dest = prefs.getString(KEY_DESTINATION, null) ?: return null
        val provider = prefs.getString(KEY_PROVIDER, null) ?: return null
        val origin = prefs.getString(KEY_ORIGIN, null)
        val lat = prefs.getString(KEY_LAT, null)
        val lng = prefs.getString(KEY_LNG, null)

        val coords = if (lat != null && lng != null) {
            try { Coordinates(lat.toDouble(), lng.toDouble()) } catch (_: Throwable) { null }
        } else null

        return PendingNavigationIntent(dest, coords, origin, provider)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    data class PendingNavigationIntent(
        val destination: String,
        val coordinates: Coordinates?,
        val origin: String?,
        val provider: String
    )

    private companion object {
        const val PREFS_NAME = "wheels_navigation_prefs"
        const val KEY_DESTINATION = "destination"
        const val KEY_LAT = "lat"
        const val KEY_LNG = "lng"
        const val KEY_ORIGIN = "origin"
        const val KEY_PROVIDER = "provider"
    }
}
