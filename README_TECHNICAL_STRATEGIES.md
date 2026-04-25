# Wheels Technical Strategies

## Scope

This document explains the main technical strategies behind the most relevant product flows currently implemented in Wheels, with emphasis on:

- Institutional login with Firebase Auth
- Session persistence
- Driver Reliability Score smart feature
- Business Question Q6 ride cancellation analytics
- Sensor-based origin autocomplete and current location suggestion
- Create Ride
- Cancel Ride
- End Ride
- Delete Ride
- Active Ride Management
- Ride group chat overlay

The goal is not to describe generic Android theory, but to define one coherent strategy for connectivity, local storage, multi-threading, and caching around the current Kotlin + Jetpack Compose + MVVM + Repository + Firebase architecture.

Some parts below are already implemented in the current codebase, while others are the recommended technical strategy to make the existing features resilient and production-ready. When that happens, the document states it explicitly.

## 1. Eventual Connectivity Strategy

The main strategy is to use an **offline-first command queue with deferred synchronization** for critical write operations.

The current app already depends on Firebase Auth, Firestore listeners, and Cloud Functions. However, for critical ride mutations we should treat the client as a producer of actions and the backend as the final source of truth. Under intermittent connectivity, the safest approach is:

- store each critical mutation locally as a pending command
- mark it as `PENDING`, `SYNCED`, or `FAILED`
- retry it automatically when connectivity returns
- keep the UI aware of local intent immediately, even if the backend confirmation arrives later

This strategy is especially important for:

- `Create Ride`
- `Cancel Ride`
- `End Ride`
- `Delete Ride`
- pending chat messages
- analytics-triggering actions such as ride cancellation

In practice, the queue would be processed with `WorkManager`, because it is the most suitable Android component for guaranteed retryable background work. The trust score and cancellation analytics do not need to be computed on device. They should remain backend-driven: once the queued Firestore write eventually reaches the backend, Cloud Functions update `trustScores`, `trustMetrics`, `user_cancellation_metrics`, and `analytics_ride_cancellations`.

This is consistent with the current design of the smart feature:

- the client writes operational ride state
- Cloud Functions observe Firestore
- the backend computes trust and analytics

So the eventual connectivity strategy is not “compute offline locally”, but rather:

> persist the user action locally, replay it reliably, and let Firebase/Cloud Functions produce the official side effects once the action reaches the backend.

To make the connectivity strategy more complete across the current Wheels flows, the command queue should be complemented with four additional eventual-connectivity mechanisms:

### 1. Optimistic UI with explicit sync state

For high-friction user actions, the UI should reflect the intended result immediately, but always with visible sync state. In practice:

- a newly created ride can appear locally as soon as the user publishes it
- a canceled ride can disappear from active actions immediately
- a completed ride can move to its archived state immediately
- a chat message can appear in the thread as soon as it is typed

However, each of those local updates should carry a state such as:

- `PENDING_SYNC`
- `SYNCED`
- `FAILED`

This reduces user confusion during poor connectivity and avoids the perception that the app “did nothing”. It is especially useful in:

- `Create Ride`
- `Cancel Ride`
- `End Ride`
- `Delete Ride`
- ride chat

### 2. Idempotent client command identifiers

Each locally queued operation should carry a stable client-generated operation id. This is important because a background retry strategy can produce duplicate submissions if the network fails mid-flight.

For example:

- `create_ride:<uuid>`
- `cancel_ride:<rideId>:<timestamp>`
- `end_ride:<rideId>:<timestamp>`
- `delete_ride:<rideId>:<timestamp>`
- `chat_message:<uuid>`

This strategy aligns very well with the backend style already used in the project, where trust and analytics logic already depend on event identity and idempotent processing. It protects:

- trust score updates from duplicated ride mutations
- analytics events from accidental duplication
- chat messages from repeated sends

### 3. Connectivity-aware retry policy by action criticality

Not all pending operations should retry in the same way. The recommended strategy is to classify actions by business criticality:

- **high criticality:** cancel ride, end ride, create ride, delete ride
- **medium criticality:** chat message delivery
- **low criticality:** non-blocking analytics logging

Then the queue can apply different retry rules:

- critical ride mutations retry more aggressively and remain visible until resolved
- chat retries can be less aggressive but keep the pending message in UI
- analytics events can retry opportunistically without disturbing the user

This makes the system more defendable than a single generic retry loop, because it recognizes that losing a ride cancellation is more severe than delaying an analytics event.

### 4. Conflict resolution with server-authoritative reconciliation

When connectivity returns, local pending state must be reconciled against the latest Firestore state rather than blindly overwriting it. The rule should be:

- Firestore remains the authoritative operational state
- the client replays commands
- once the server response or snapshot arrives, local cache is reconciled to the remote truth

This matters in shared scenarios such as:

- a ride gets canceled from another client before this device retries `end ride`
- a ride is deleted remotely while this client still shows it cached
- the passenger sees a cached published ride that is no longer available

For those cases, the app should not insist on local stale intent. It should:

- mark the operation as failed or obsolete
- refresh the local cache from Firestore
- show the reconciled state to the user

This is especially relevant in Wheels because Android and Flutter clients share the same backend collections.

| Feature / Flow | Strategy Applied |
|---|---|
| Institutional login with Firebase Auth | Do not queue authentication itself. If login fails due to connectivity, surface the error immediately and retry explicitly. |
| Session persistence | Keep the last valid authenticated session locally so the user does not lose access state when the app restarts offline. |
| Create Ride | Proposed queued write with optimistic UI, `PENDING/SYNCED/FAILED` sync state, and an idempotent operation id so the ride is not created twice during retry. |
| Cancel Ride flow | Proposed queued mutation with high-priority retry, visible pending state, and backend reconciliation so trust score and analytics update once the cancel event reaches Firestore. |
| End Ride flow | Proposed queued completion command with high-priority retry and server-authoritative reconciliation in case another client changed the ride before sync completes. |
| Delete Ride flow | Proposed queued delete with optimistic local removal, but final confirmation only after Firestore acknowledges deletion or snapshots reconcile the result. |
| Active Ride Management | Read state can remain snapshot-driven; critical actions inside the screen should enqueue mutations, expose sync status, and reconcile against the latest shared backend state. |
| Ride group chat overlay | Proposed queued outbound messages with local pending rendering, idempotent message ids, and medium-priority retry policy. |
| Driver Reliability Score smart feature | Already backend-driven. The client should not compute the score offline; it should wait for queued ride-state changes to reach Firestore and then consume the updated trust score snapshot. |
| Business Question Q6 analytics | Already backend-driven. Cancellation analytics are eventually produced when the cancel event is successfully written and Cloud Functions process it; analytics retries should be low-priority and non-blocking. |
| Sensor-based current location suggestion | No queue is needed. If location lookup fails offline or by permission denial, fall back gracefully without blocking the form. |

## 2. Local Storage Strategy

The main strategy is to use **DataStore for lightweight session/preference state and Room for structured offline data and pending operations**, while keeping Firebase as the remote source of truth.

This fits the current project structure well:

- authentication and role/session state are small, preference-like pieces of data
- rides, queued mutations, recent chat messages, and sync metadata are structured entities
- trust score and analytics remain backend-owned and should not be treated as authoritative local business state

Under this strategy:

- `DataStore` stores lightweight app/session state
- `Room` stores structured records that need querying, retry, or offline continuity
- Firestore remains the canonical remote store

This prevents overloading one storage mechanism with responsibilities it does not fit.

| Data / Feature | Local Storage Strategy |
|---|---|
| Auth session | DataStore for lightweight persisted session metadata and UI restore behavior; Firebase Auth remains the real auth authority. |
| Active role / roles | DataStore can cache the last active role for fast UI restore; Firestore `users/{uid}` remains the authoritative profile source. |
| Basic profile | Cache minimal profile data locally for startup continuity; refresh from Firestore on session restore. |
| Pending ride commands | Room table for queued commands such as create, cancel, end, and delete ride, including sync status and retry metadata. |
| Rides recently loaded in passenger and driver flows | Room cache for recently observed rides and active ride state, especially useful for `My Rides` and passenger browsing under unstable connectivity. |
| Active Ride Management | Room snapshot of the current active ride and participants to avoid blank screens while the network reconnects. |
| Trust score last known value | Store the last observed trust score locally for quick rendering, but treat Firestore as the final source of truth. |
| Q6 aggregated cancellation metrics | Optional local read cache only; backend-generated collections remain authoritative. |
| Chat pending messages | Room table with message status (`PENDING`, `SYNCED`, `FAILED`) to support optimistic chat UI and replay. |
| Current selected origin/destination in Create Ride | DataStore or Room draft state, depending on how many draft fields are persisted. This is useful for accidental process death or app restarts. |
| Raw analytics dashboard state | Do not cache Looker/BigQuery dashboards in app. The client only logs operational events and reads app-facing aggregates from Firestore. |

## 3. Multi-threading Strategy

The main strategy is to use **Kotlin Coroutines with `Dispatchers.IO` for I/O-bound work, `ViewModelScope` for screen-bound orchestration, and `Flow` / `StateFlow` for reactive state propagation to Compose**.

This is already strongly aligned with the project:

- Compose UI is driven by `StateFlow`
- repositories expose reactive streams
- Firestore listeners are wrapped as `Flow`
- ViewModels coordinate screen state

The key rule is:

> never block the main thread with Firebase, Room, location, sync, or analytics work.

Instead:

- repositories perform Firebase/Firestore and future Room work on `Dispatchers.IO`
- ViewModels launch coroutines in `viewModelScope`
- Compose only observes state and renders on the main thread
- background sync is delegated to `WorkManager`, which internally runs its work off the UI thread

This strategy is especially defendable because the app already mixes:

- remote data listeners
- asynchronous trust score updates
- location access
- form flows
- analytics logging

All of those are naturally modeled as asynchronous operations.

| Operation | Threading Strategy |
|---|---|
| Firebase Auth sign-in | Launch from `ViewModelScope`; actual auth call runs asynchronously and must not block the UI thread. |
| Session restore | Observe from ViewModel and propagate to UI through `StateFlow`; lightweight persistence can be read off the main thread if needed. |
| Firestore listeners for rides and trust score | Wrap callback-based listeners in `Flow` / `callbackFlow`, process mapping on `Dispatchers.IO`, and emit UI state reactively. |
| Create Ride / Cancel Ride / End Ride / Delete Ride | Trigger from `ViewModelScope`, execute repository and future queue persistence on `Dispatchers.IO`. |
| Trust score observation | Keep as reactive `Flow`; the UI consumes the last emitted score without polling. |
| Cancellation behavior metrics and nudges | Collect asynchronously in the ViewModel and derive UI state from the observed backend aggregates. |
| Location access | Run location lookup asynchronously; never block the Compose thread waiting for GPS or geocoding. |
| Analytics logging | Fire-and-forget from coroutine context or background worker; failures should not freeze the UI. |
| Room reads/writes | Run on `Dispatchers.IO`; expose query results as `Flow` to integrate naturally with ViewModels. |
| Sync queue processing | Use `WorkManager` plus coroutines so retries and batch processing happen in the background without screen lifetime dependency. |
| Chat messages | Use coroutines for send/receive/update status; UI observes the resulting `Flow` state. |

## 4. Caching Strategy

The main strategy is to use **local read-through caching for frequently accessed app data, with snapshot-driven invalidation and Firestore/backend as the authoritative source**.

The app already relies heavily on reactive remote updates. The most coherent caching strategy is therefore:

- show cached data immediately when available
- subscribe to Firestore or repository flows
- replace or invalidate cached entries when newer remote data arrives
- keep trust/analytics as derived backend-owned state rather than client-owned state

This strategy is particularly appropriate for Wheels because:

- many screens are list/detail based
- users revisit the same ride and profile contexts
- trust score is read frequently but written only by the backend
- origin suggestions and recent rides are ideal cache candidates

The cache should be pragmatic:

- accelerate read-heavy flows
- support temporary offline continuity
- avoid pretending to be the final authority over trust or analytics

| Data | Caching Strategy |
|---|---|
| Current authenticated user/session | Cache the last known session/profile snapshot locally for quick app startup; refresh once Firebase/Auth and Firestore are available. |
| Profile | Cache last known profile fields and role state locally; invalidate on new Firestore snapshot or explicit profile mutation. |
| Passenger rides list | Cache the last fetched visible rides list locally, show it immediately, then refresh from Firestore snapshots. |
| Driver `My Rides` list | Cache recent driver rides locally for continuity, but replace from live Firestore updates as soon as they arrive. |
| Active ride | Cache the last active ride locally because it is central to ride management and should reopen quickly after process death or temporary disconnect. |
| Trust score | Cache only the latest observed score for rendering continuity; invalidate as soon as a new `trustScores/{userId}` snapshot arrives. |
| Cancellation behavior metrics | Cache the latest aggregate values for nudges, but always treat backend-written aggregates as authoritative. |
| Origin suggestions / current location label | Cache recent suggestions and the last selected origin locally to reduce friction in Create Ride; refresh current location only when explicitly requested. |
| Chat recent messages | Cache recent conversation history locally and merge with remote updates; pending outbound messages stay marked until acknowledged. |
| Analytics / dashboard data | Do not cache BI dashboards in the app. The app should only cache app-facing operational or aggregate data that directly affects UX. |

## 5. Summary

The most coherent technical position for Wheels is:

- **Eventual connectivity:** use a local pending-action queue with background replay for critical ride and chat mutations
- **Local storage:** use `DataStore` for lightweight session/preferences and `Room` for structured offline entities and pending commands
- **Multi-threading:** standardize on Kotlin Coroutines, `Dispatchers.IO`, `ViewModelScope`, and `Flow` / `StateFlow`
- **Caching:** cache frequently read app-facing data locally, but keep Firebase Auth, Firestore, and Cloud Functions as the remote sources of truth

This strategy is technically defendable because it matches the current architecture already in place:

- Kotlin + Jetpack Compose
- MVVM
- Repository Pattern
- Firebase Auth
- Firestore
- Cloud Functions
- BigQuery / Looker for analytics
- feature-based clean-ish modularization

It also fits the implemented smart features correctly:

- the client captures user intent
- repositories abstract remote access
- ViewModels expose reactive UI state
- backend services compute trust and analytics
- local persistence and caching provide resilience without duplicating backend business logic

That balance is the core technical strategy of Wheels: **reactive client, reliable local continuity, and backend-owned business truth**.
