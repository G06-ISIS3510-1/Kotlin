# Analytics Design

This feature scaffolding computes most frequent user destinations using a clean architecture split between domain contracts and data implementations.

`DestinationAnalyticsService` defines the app-facing analytics contract, `AnalyticsRepository` exposes the feature data boundary, and `FirebaseAnalyticsAdapter` isolates Firebase-specific details from the rest of the app.

Implementation is intentionally pending for the next iteration, so the current files only define structure, models, and placeholder methods.
