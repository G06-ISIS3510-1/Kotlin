package com.wheels.app.features.rides.data.adapter

import com.wheels.app.features.rides.data.remote.GoogleMapsClient
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.PlaceResult
import com.wheels.app.features.rides.domain.service.GeocodingService
import javax.inject.Inject

class GoogleMapsAdapter @Inject constructor(
    private val googleMapsClient: GoogleMapsClient
) : GeocodingService {

    // Adapter keeps the rides feature isolated from provider-specific map client details.
    override suspend fun getCoordinates(address: String): Coordinates {
        TODO("Pending mapping from GoogleMapsClient.geocode response into Coordinates.")
    }

    override suspend fun getAddress(lat: Double, lng: Double): String {
        TODO("Pending mapping from GoogleMapsClient.reverseGeocode response into a display address.")
    }

    override suspend fun autocomplete(query: String): List<PlaceResult> {
        TODO("Pending mapping from GoogleMapsClient.placesAutocomplete response into PlaceResult values.")
    }
}
