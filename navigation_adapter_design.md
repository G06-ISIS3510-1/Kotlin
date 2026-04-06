# Navigation Adapter Design

This navigation integration belongs to the `rides` feature.

The project follows clean architecture and a feature-based organization, so the rides feature depends on `NavigationService` as a domain abstraction instead of depending directly on Google Maps or Waze APIs.

Google Maps and Waze are integrated through separate adapters in the data layer:

- `GoogleMapsNavigationAdapter`
- `WazeNavigationAdapter`

This uses the Adapter pattern to reduce coupling between the rides feature and provider-specific URI formats, package names, and deep-link behavior.

Navigation is delegated to specialized external apps instead of embedding maps or turn-by-turn navigation inside the app. This keeps the mobile project simpler, more maintainable, and aligned with the scope of an academic implementation.
