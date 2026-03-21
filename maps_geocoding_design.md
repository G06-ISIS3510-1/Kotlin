# Maps & Geocoding Design

This rides feature scaffolding uses the Adapter pattern.

`GoogleMapsAdapter` isolates the rides feature from provider-specific map and geocoding API details by exposing the app-facing `GeocodingService` contract while delegating provider work to `GoogleMapsClient`.

Implementation is intentionally pending for the next iteration, so the current files only define structure, contracts, and placeholder methods.
