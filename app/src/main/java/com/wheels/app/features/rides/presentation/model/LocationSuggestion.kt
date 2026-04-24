package com.wheels.app.features.rides.presentation.model

import com.wheels.app.features.rides.domain.model.Coordinates

data class LocationSuggestion(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val coordinates: Coordinates? = null,
    val type: LocationSuggestionType = LocationSuggestionType.PLACE
)

enum class LocationSuggestionType {
    CURRENT_LOCATION,
    PLACE
}
