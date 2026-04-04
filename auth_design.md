# Authentication Design

This feature scaffolding supports institutional login and future session persistence for the authentication flow.

`InstitutionalAuthAdapter` uses the Adapter pattern to isolate the app from provider-specific authentication services while exposing the app-facing `AuthService` contract.

Implementation is intentionally pending for the next iteration, so the current files only define the base structure, models, and placeholder methods.
