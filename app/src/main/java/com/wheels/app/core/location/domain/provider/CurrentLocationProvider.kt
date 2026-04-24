package com.wheels.app.core.location.domain.provider

import com.wheels.app.core.location.domain.model.CurrentLocationLabel
import com.wheels.app.features.rides.domain.model.Coordinates

interface CurrentLocationProvider {
    suspend fun getCurrentLocationLabel(): CurrentLocationLabel
    suspend fun getCurrentCoordinates(): Coordinates
}
