package com.wheels.app.features.rides.domain.service

import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.PlaceResult

interface GeocodingService {
    suspend fun getCoordinates(address: String): Coordinates

    suspend fun getAddress(lat: Double, lng: Double): String

    suspend fun autocomplete(query: String): List<PlaceResult>
}
