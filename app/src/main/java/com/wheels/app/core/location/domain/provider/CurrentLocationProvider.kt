package com.wheels.app.core.location.domain.provider

import com.wheels.app.core.location.domain.model.CurrentCoordinates
import com.wheels.app.core.location.domain.model.CurrentLocationLabel

interface CurrentLocationProvider {
    suspend fun getCurrentLocationLabel(): CurrentLocationLabel
    suspend fun getCurrentCoordinates(): CurrentCoordinates
    suspend fun geocodeAddress(address: String): CurrentCoordinates?
}
