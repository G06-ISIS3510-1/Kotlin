package com.wheels.app.features.rides.domain.model

data class PlaceResult(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)
