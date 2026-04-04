package com.wheels.app.features.rides.data.remote

import javax.inject.Inject

class GoogleMapsClient @Inject constructor() {
    suspend fun geocode(address: String): String {
        throw NotImplementedError("Pending API integration for geocoding.")
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): String {
        throw NotImplementedError("Pending API integration for reverse geocoding.")
    }

    suspend fun placesAutocomplete(query: String): String {
        throw NotImplementedError("Pending API integration for places autocomplete.")
    }
}
