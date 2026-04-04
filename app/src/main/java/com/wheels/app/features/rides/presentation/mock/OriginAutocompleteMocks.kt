package com.wheels.app.features.rides.presentation.mock

import com.wheels.app.features.rides.presentation.model.LocationSuggestion
import com.wheels.app.features.rides.presentation.model.LocationSuggestionType

object OriginAutocompleteMocks {
    val currentLocationSuggestion = LocationSuggestion(
        id = "current-location",
        title = "Use current location",
        type = LocationSuggestionType.CURRENT_LOCATION
    )

    val locationSuggestions = listOf(
        LocationSuggestion(id = "uniandes", title = "Universidad de los Andes"),
        LocationSuggestion(id = "chapinero", title = "Chapinero"),
        LocationSuggestion(id = "usaquen", title = "Usaquen"),
        LocationSuggestion(id = "cedritos", title = "Cedritos"),
        LocationSuggestion(id = "portal-norte", title = "Portal Norte"),
        LocationSuggestion(id = "calle-100", title = "Calle 100"),
        LocationSuggestion(id = "home", title = "Home"),
        LocationSuggestion(id = "work", title = "Work")
    )
}
