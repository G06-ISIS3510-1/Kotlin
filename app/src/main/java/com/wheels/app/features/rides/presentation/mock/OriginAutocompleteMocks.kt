package com.wheels.app.features.rides.presentation.mock

import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.presentation.model.LocationSuggestion
import com.wheels.app.features.rides.presentation.model.LocationSuggestionType

object OriginAutocompleteMocks {
    val currentLocationSuggestion = LocationSuggestion(
        id = "current-location",
        title = "Use current location",
        type = LocationSuggestionType.CURRENT_LOCATION
    )

    val locationSuggestions = listOf(
        LocationSuggestion(
            id = "uniandes",
            title = "Universidad de los Andes",
            subtitle = "Cra. 1 #18A-12, Bogota",
            coordinates = Coordinates(lat = 4.60157, lng = -74.06518)
        ),
        LocationSuggestion(
            id = "chapinero",
            title = "Chapinero",
            subtitle = "Bogota",
            coordinates = Coordinates(lat = 4.64862, lng = -74.06283)
        ),
        LocationSuggestion(
            id = "usaquen",
            title = "Usaquen",
            subtitle = "Bogota",
            coordinates = Coordinates(lat = 4.69574, lng = -74.03032)
        ),
        LocationSuggestion(
            id = "cedritos",
            title = "Cedritos",
            subtitle = "Bogota",
            coordinates = Coordinates(lat = 4.72114, lng = -74.04668)
        ),
        LocationSuggestion(
            id = "portal-norte",
            title = "Portal Norte",
            subtitle = "Autopista Norte, Bogota",
            coordinates = Coordinates(lat = 4.75458, lng = -74.04686)
        ),
        LocationSuggestion(
            id = "calle-100",
            title = "Calle 100",
            subtitle = "Bogota",
            coordinates = Coordinates(lat = 4.68372, lng = -74.05464)
        ),
        LocationSuggestion(
            id = "home",
            title = "Home",
            subtitle = "Saved place",
            coordinates = Coordinates(lat = 4.66989, lng = -74.05579)
        ),
        LocationSuggestion(
            id = "work",
            title = "Work",
            subtitle = "Saved place",
            coordinates = Coordinates(lat = 4.65844, lng = -74.09412)
        )
    )
}
