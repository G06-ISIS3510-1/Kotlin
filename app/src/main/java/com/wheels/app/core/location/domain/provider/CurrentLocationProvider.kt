package com.wheels.app.core.location.domain.provider

import com.wheels.app.core.location.domain.model.CurrentLocationLabel

interface CurrentLocationProvider {
    suspend fun getCurrentLocationLabel(): CurrentLocationLabel
}
