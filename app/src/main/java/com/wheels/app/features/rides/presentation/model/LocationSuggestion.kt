package com.wheels.app.features.rides.presentation.model

data class LocationSuggestion(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: LocationSuggestionType = LocationSuggestionType.PLACE
)

enum class LocationSuggestionType {
    CURRENT_LOCATION,
    PLACE
}
