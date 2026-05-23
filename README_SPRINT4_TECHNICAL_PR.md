# Sprint 4 Technical README
## Wheels Android App

## Overview

This document summarizes the technical design, architectural decisions, and implementation rationale for Sprint 4 in the Wheels mobile application. The goal of this sprint was to evolve the app from a primarily online-first mobile client into a more resilient, connectivity-aware system capable of preserving critical user workflows under unstable or missing network conditions.

Sprint 4 focused on eventual connectivity as a product and architectural concern. Instead of treating network loss as an exceptional failure case, the application now treats connectivity as a fluctuating runtime condition and adapts behavior accordingly. The result is a more robust mobile-first experience where users can keep interacting with core workflows even when Firebase services are temporarily unreachable.

The implementation was designed around four core eventual connectivity scenarios:

1. Offline session continuity.
2. Create Ride offline draft plus deferred publish.
3. Active Ride Management offline actions with deferred synchronization.
4. Ride list and search cache fallback.

Additionally, Sprint 4 introduced a business analytics question around usage of the `Use current location` feature during ride creation. This was integrated into the project using Firestore and Cloud Functions as part of an event-driven analytics pipeline.

This README is intentionally written as a technical PR companion. It explains the architectural intent, how the current solution fits into the existing codebase, what patterns were used, why those choices were made, and what tradeoffs were accepted.

---

# 1. Introduction

## 1.1 Sprint 4 objective

The main objective of Sprint 4 was to increase application resilience under intermittent connectivity. In mobile environments, assuming stable network access is rarely realistic. Users may lose Wi-Fi, move between cellular coverage zones, experience temporary DNS resolution failures, or intentionally operate the device offline. A transport application such as Wheels cannot assume that ride-related flows will always execute in a fully online context.

Sprint 4 therefore focused on enabling continuity in workflows that are central to the product:

- preserving authenticated access when the app is reopened offline,
- allowing drivers to continue preparing and publishing rides even without connectivity,
- allowing operational ride actions to be captured and synchronized later,
- allowing passengers and drivers to continue seeing previously retrieved ride data.

## 1.2 Resilience as a mobile requirement

Resilience in this context does not mean perfect offline parity for every backend feature. Instead, it means that the app gracefully degrades while protecting user intent.

Examples of protected intent include:

- a user who was already signed in should not be blocked from re-entering the app only because Firebase is unreachable,
- a driver who fills a ride form should not lose that work due to a network interruption,
- a driver who decides to start, complete, or cancel a ride offline should not have to remember to retry later,
- a passenger who already loaded a ride list should not be forced into an empty screen if the network drops during the same session.

## 1.3 Eventual connectivity

The architectural model adopted in Sprint 4 is eventual connectivity.

Eventual connectivity assumes:

- network access is not guaranteed at every moment,
- some operations can be safely deferred,
- local intent can be persisted,
- synchronization can happen later when connectivity is restored,
- the backend remains the long-term source of truth.

This differs from naive retry logic. Instead of repeatedly failing online calls, the app records local intent, preserves the necessary state, and replays operations through controlled synchronization flows.

## 1.4 Offline continuity as a product decision

Offline continuity was treated as a product capability, not merely a technical fallback. This is why the implementation includes not only storage and synchronization, but also:

- UI feedback,
- pending states,
- navigation continuity,
- cache-backed list rendering,
- explicit synchronization lifecycle messaging.

## 1.5 Mobile-first architecture

A mobile-first architecture recognizes that the device itself is a durable execution environment with local memory, local persistence, intermittent transport, and user expectations of continuity.

For that reason, Sprint 4 deliberately combines:

- local key-value persistence with DataStore,
- relational local storage with Room,
- in-memory hot caches with LruCache,
- asynchronous orchestration with coroutines and Flow,
- deferred reconciliation with Firebase and Cloud Functions.

The result is not full offline-first replication of the backend, but a pragmatic connectivity-aware architecture aligned with product priorities.

---

# 2. Current Architecture

## 2.1 Architectural baseline

The Wheels app uses a Kotlin Android stack with:

- Jetpack Compose for UI,
- MVVM for presentation state management,
- Repository Pattern for data orchestration,
- Firebase Auth for authentication,
- Firestore for remote data,
- Cloud Functions for backend event processing,
- feature-based project organization,
- coroutine-based asynchronous execution.

Sprint 4 did not replace this architecture. Instead, it extended it by adding local persistence, memory caching, and synchronization flows in a way that preserves the existing structure.

## 2.2 MVVM

The presentation layer uses ViewModels as state coordinators. UI composables remain declarative and render state derived from `StateFlow` or collected `Flow` streams.

This was important for eventual connectivity because ViewModels became the place where we could:

- observe connectivity,
- collect locally persisted state,
- trigger synchronization when network returns,
- expose pending or cached states to the UI,
- avoid placing backend or persistence logic directly in composables.

## 2.3 Repository Pattern

Repositories are the integration layer between domain-facing use cases and concrete data sources.

In Sprint 4, repositories became the most important orchestration boundary because they coordinate:

- Firestore,
- Room,
- DataStore-backed session persistence,
- in-memory caches,
- network availability checks,
- retry and synchronization behavior.

The repository layer therefore evolved from a simple remote adapter into a connectivity-aware orchestration layer.

## 2.4 Feature-based organization

The project remains organized by feature, which helps keep offline logic close to the workflows it affects. For example:

- auth-related continuity remains inside auth-related packages,
- ride creation persistence remains inside rides-related packages,
- local cache abstractions for ride lists remain inside rides/data/local,
- synchronization logic remains near the ride repository rather than in a global connectivity service.

This improves maintainability because offline strategy stays contextual to the feature.

## 2.5 Clean-ish layering

The project follows a practical clean-ish layering rather than strict clean architecture.

Typical layers are:

- presentation,
- domain models and repository contracts,
- data implementations,
- local and remote data sources.

This separation was useful in Sprint 4 because it allowed us to add new storage and cache mechanisms without leaking those details into the UI.

## 2.6 Flow and StateFlow

Flow and StateFlow are essential to this architecture.

They are used to:

- observe authentication state,
- observe Room-backed draft state,
- observe pending queues,
- react to connectivity changes,
- stream Firestore snapshots,
- merge local and remote sources over time.

Reactive streams are a natural fit for eventual connectivity because the state of the app can transition through several connectivity-aware phases:

- remote fresh data,
- cached fallback,
- pending offline action,
- later synchronized remote truth.

## 2.7 Firebase backend and event-driven approach

Firebase remains the remote system of record for the application. Firestore stores operational entities such as rides, and Cloud Functions react to backend events.

This event-driven posture is important for two reasons:

1. The mobile app can remain focused on recording intent and rendering state.
2. Backend consistency logic and analytics generation can remain centralized.

The analytics question implemented in Sprint 4 uses this model: the app writes current-location usage flags into the ride document, and a Cloud Function projects that information into an analytics collection.

---

# 3. Eventual Connectivity Strategies

## 3.1 Offline session continuity with DataStore

### Problem

Without additional persistence, authentication continuity depends on Firebase Auth being reachable or on the process still holding state. This creates a poor experience when the app is closed and later reopened without internet.

A user who was already signed in should not be locked out of the app only because the device is currently offline.

### Strategy

The chosen strategy was to persist the last valid authenticated session locally using DataStore. This local session acts as a lightweight continuity mechanism that can reconstruct the essential authenticated user state during offline startup.

### Core flow

1. User signs in successfully online.
2. Repository builds the app-level authenticated user model.
3. Session is serialized into local DataStore.
4. App is later reopened without connectivity.
5. Repository reads the last stored session.
6. ViewModel exposes restored session state.
7. Navigation allows access to protected screens.

### Components involved

- `AuthSessionLocalStore`
- `AuthRepositoryImpl`
- `AuthSessionViewModel`
- `WheelsNavGraph`
- `SessionTransitionCoordinator`

### Architectural decisions

DataStore was chosen because this strategy needs:

- small structured data,
- persistence between launches,
- very low operational complexity,
- no relational queries,
- fast read/write semantics.

A Room database would be unnecessarily heavy for this purpose.

### Advantages

- Protected screens remain accessible offline after a prior valid login.
- Session continuity survives process death and app restart.
- Logout can explicitly clear the persisted session.

### Tradeoffs

- This is continuity, not token refresh.
- Backend trust still depends on Firebase once connectivity returns.
- Persisted session must remain minimal and carefully cleared on logout.

## 3.2 Create Ride offline draft plus deferred publish

### Problem

Ride creation is a multi-field user workflow. Without local draft storage, any interruption risks losing form progress. Without deferred publish, a driver attempting to publish offline would receive failure feedback and potentially abandon the action.

### Strategy

The ride creation workflow was split into two local resilience layers:

1. Draft persistence while editing.
2. Deferred publish queue when a publish is attempted offline.

### Draft persistence flow

1. Driver edits fields in the Create Ride form.
2. ViewModel converts current UI state into a `CreateRideDraft` domain model.
3. Repository stores that draft in Room through a DAO.
4. If the app closes, the draft remains durable.
5. On reopening the screen, the draft is reloaded from Room and restored into UI state.

### Deferred publish flow

1. Driver presses Publish Ride while offline.
2. ViewModel builds a `PublishRideRequest`.
3. Repository stores it in Room as a pending publish record.
4. UI reflects that the ride is saved locally and will publish later.
5. When connectivity returns, the pending publish queue is replayed.
6. Successful publishes are removed from Room.

### Components involved

- `RidesViewModel`
- `RideRepositoryImpl`
- `RideOfflineDao`
- `CreateRideDraftEntity`
- `PendingRidePublishEntity`
- `WheelsDatabase`

### Architectural decisions

This strategy deliberately uses Room instead of DataStore because:

- drafts contain multiple typed fields,
- pending publishes represent a queue structure,
- failure metadata and retry state are easier to represent in relational storage,
- the lifecycle of these records is durable and transactional in spirit.

### Advantages

- Form state survives process death.
- Publish intent is never lost simply because the device is offline.
- UI can clearly differentiate between published rides and pending-to-publish rides.

### Tradeoffs

- Local queue management adds complexity.
- Schema evolution must be managed through Room versioning.
- Deferred publish means temporary divergence from backend state.

## 3.3 Active Ride Management deferred synchronization

### Problem

Operational ride actions such as start, complete, cancel, or delete are time-sensitive from the user perspective. If the user performs them offline and the app simply fails, the workflow becomes brittle and the user loses trust.

### Strategy

Offline ride actions are captured as pending commands in Room and replayed later.

### Flow

1. Driver taps an operational action.
2. ViewModel checks connectivity.
3. If online, the repository performs the remote operation immediately.
4. If offline, the action is converted into a `PendingRideAction` and stored in Room.
5. UI reflects optimistic or pending state.
6. When internet returns, the repository synchronizes queued actions in order.
7. Successful actions are deleted from the queue.
8. Failed actions are marked with retry metadata.

### Components involved

- `RidesViewModel`
- `RideRepositoryImpl`
- `RideOfflineDao`
- `PendingRideActionEntity`
- `PendingRideAction`
- trust repository integration

### Architectural decisions

This strategy was modeled as a command queue rather than a raw state mirror. That was important because these actions are not just passive data snapshots; they represent user intent with ordering and replay semantics.

### Advantages

- The user can keep operating on rides without internet.
- Synchronization is explicit and trackable.
- This aligns well with command-based eventual consistency.

### Tradeoffs

- Requires careful control of duplicate actions.
- Introduces temporary mismatch between UI state and backend truth.
- Trust score effects must be reconciled carefully after sync.

## 3.4 Ride search and list cache fallback

### Problem

Ride browsing is a read-heavy feature. Requiring fresh network access for every visit would lead to empty states as soon as connectivity drops, even if useful data was loaded moments before.

### Strategy

Use LruCache as a session-scoped in-memory fallback for:

- passenger available rides,
- nearby rides,
- driver ride lists.

### Flow

1. Repository attempts to read cache first.
2. If cache exists, it can emit immediately.
3. Repository checks connectivity.
4. If online, it refreshes remote data and rewrites the cache.
5. If offline, it returns the cached data if available.

### Components involved

- `AvailableRidesLocalCache`
- `DriverRidesLocalCache`
- `NearRidesLocalCache`
- `RideRepositoryImpl`

### Architectural decisions

This strategy uses LruCache because the targeted data is:

- read-mostly,
- session-scoped,
- non-critical to persist after process death,
- beneficial to retrieve quickly from memory.

### Advantages

- Fast access to hot data.
- Reduced unnecessary network dependency within the same app session.
- Clear conceptual separation between cache and persistence.

### Tradeoffs

- Cache is lost when the process dies.
- This is not durable offline storage.
- Size constraints matter in Android memory environments.

---

# 4. Local Storage

## 4.1 Why Room

Room was selected for workflows that require durable structured persistence.

This includes:

- ride creation drafts,
- pending ride publishes,
- pending ride actions.

These data structures are more than single preferences. They have identity, multiple typed fields, queue semantics, and in some cases retry metadata.

## 4.2 Why DataStore

DataStore was selected for the persisted authenticated session because that problem is fundamentally key-value and lightweight. The session continuity layer does not require joins, queue semantics, or relational access patterns.

## 4.3 Room versus DataStore

Room is appropriate when:

- data has structure,
- data has identity,
- records may be queried or observed,
- queue-like or transactional behavior matters.

DataStore is appropriate when:

- data is lightweight,
- there are few records,
- the access pattern is simple,
- the model resembles preferences or a serialized small object.

## 4.4 Room entities introduced

Sprint 4 relies on entities such as:

- `CreateRideDraftEntity`
- `PendingRidePublishEntity`
- `PendingRideActionEntity`

These represent the durable local state necessary to preserve user intent under connectivity loss.

## 4.5 DAO responsibilities

The DAO layer is responsible for direct SQL-backed access patterns such as:

- observing drafts,
- upserting drafts,
- clearing drafts,
- inserting pending publishes,
- retrieving pending publish queues,
- inserting pending actions,
- retrieving pending action queues,
- deleting or marking failed queued records.

The DAO is intentionally not responsible for orchestration logic. It should not decide when synchronization happens or when connectivity is sufficient.

## 4.6 Queue semantics

Pending publishes and pending ride actions behave like local command queues.

Important properties:

- they are durable,
- they represent intent that has not yet been reconciled remotely,
- they can be replayed later,
- they support error marking and retry.

## 4.7 Persistence lifecycle

A local persistence lifecycle in Sprint 4 typically follows this pattern:

1. User action happens.
2. ViewModel decides local or remote path.
3. Repository persists data locally if remote execution is not possible.
4. UI reflects pending status.
5. Connectivity observer triggers synchronization later.
6. Repository reconciles with backend.
7. Local records are deleted or marked failed.

This lifecycle is central to eventual connectivity.

---

# 5. Caching

## 5.1 Why LruCache

LruCache is a good fit for session-scoped read acceleration in Android because it provides bounded in-memory caching with a simple eviction strategy.

The acronym LRU stands for Least Recently Used. This reflects the temporal locality assumption that recently accessed data is more likely to be accessed again soon.

## 5.2 Temporal locality

Temporal locality is especially relevant for mobile list screens.

Examples in Wheels:

- a passenger returns to the ride list soon after browsing it,
- a driver revisits My Rides repeatedly in the same session,
- a nearby rides query is likely to be revisited shortly after it was loaded.

In these cases, keeping the most recently used data in memory provides immediate product value.

## 5.3 Eviction policy

LRU eviction removes the least recently used entry when the configured capacity is exceeded.

This is appropriate because:

- mobile memory is limited,
- we do not want unbounded caches,
- not all list snapshots are equally valuable,
- recent user context is typically the best predictor of near-future reuse.

## 5.4 Why cache size matters

Choosing cache size is an architectural decision, not an implementation detail.

If the cache is too small:

- reuse opportunities are lost,
- churn increases,
- fallback value decreases.

If the cache is too large:

- memory pressure increases,
- process stability may degrade,
- the cache may retain data that is unlikely to be reused.

Sprint 4 uses targeted cache sizes rather than trying to cache the entire remote world.

## 5.5 DriverRidesLocalCache as a valid use case

`DriverRidesLocalCache` is a valid LRU use case because:

- driver ride lists are revisited frequently,
- they are read-heavy compared to write-heavy,
- they are useful during the same app session,
- they do not necessarily need to survive process death,
- they complement, rather than replace, Room queues.

This makes it a strong example of a real memory cache rather than pseudo-persistence.

## 5.6 Cache versus persistence

This distinction is important for viva voce defense.

Cache means:

- data is temporary,
- data lives in memory,
- data improves performance or short-term continuity,
- data may disappear when the process dies.

Persistence means:

- data survives process death,
- data is stored durably,
- data is part of continuity guarantees,
- data can support deferred synchronization.

In Wheels:

- LruCache supports short-term continuity and fast reads,
- Room supports durable offline workflows,
- DataStore supports durable lightweight session continuity.

---

# 6. Multithreading

## 6.1 Why I/O must not run on Main

Android UI rendering happens on the main thread. If database reads, Firestore calls, cache serialization, or synchronization loops block Main, the result is:

- dropped frames,
- frozen UI,
- poor input responsiveness,
- ANR risk.

For that reason, Sprint 4 explicitly keeps I/O-bound work off Main.

## 6.2 Dispatchers.IO

`Dispatchers.IO` is used for work such as:

- Room access,
- queue processing,
- cache access when treated as storage-bound or coordination-bound work,
- synchronization loops.

The repository layer typically uses `withContext(ioDispatcher)` or `flowOn(ioDispatcher)` to ensure these operations happen on the appropriate dispatcher.

## 6.3 Coroutines

Kotlin coroutines are the concurrency foundation for Sprint 4.

They allow the app to:

- launch non-blocking tasks from ViewModels,
- switch execution contexts for I/O,
- compose asynchronous flows of work,
- preserve readable imperative code while still remaining asynchronous.

## 6.4 Flow and asynchronous streaming

Flow is used for:

- reactive reads from Room,
- reactive Firestore listeners,
- connectivity-driven updates,
- downstream UI updates from repository streams.

This is particularly useful in eventual connectivity scenarios where state changes over time and the UI must react to transitions rather than one-time responses.

## 6.5 Repository orchestration

Repositories are where multithreading becomes meaningful in architectural terms.

Typical orchestration looks like:

1. ViewModel launches coroutine.
2. Repository checks connectivity.
3. Repository reads cache or Room on I/O.
4. Repository decides remote or local path.
5. Repository emits Flow or returns result.
6. UI updates without blocking Main.

## 6.6 Why this matters for Sprint 4

Eventual connectivity features are naturally asynchronous. They involve:

- persistence,
- background synchronization,
- stream observation,
- retry and replay logic.

Trying to implement this without coroutines and dispatcher separation would quickly make the app either unsafe for UI or much harder to reason about.

---

# 7. Repository Layer

## 7.1 Why repositories orchestrate multiple data sources

The repository layer is the correct place to orchestrate:

- Room,
- Firebase,
- caches,
- connectivity checks.

This is because none of those concerns belongs directly in the UI, and DAOs should remain focused on persistence mechanics rather than product flow coordination.

## 7.2 DAO versus Repository

A DAO is a persistence boundary.

It answers questions such as:

- how do we insert a pending publish?
- how do we observe a draft?
- how do we delete a queued action?

A repository is an orchestration boundary.

It answers questions such as:

- should this operation go to Firestore or Room?
- should cache be used before remote fetch?
- when should synchronization happen?
- what should be returned if the device is offline?

## 7.3 Separation of concerns

Repositories in Sprint 4 keep the following responsibilities separate:

- DAOs manage local persistence mechanics,
- caches manage hot in-memory snapshots,
- Firebase handles remote truth,
- ViewModels coordinate presentation state,
- repositories choose and combine those data sources.

This separation is what keeps the system explainable and maintainable.

## 7.4 Why RideRepositoryImpl is central

`RideRepositoryImpl` became the core integration point for Sprint 4 because the rides feature is where eventual connectivity matters the most operationally.

It now contains strategy-specific sections for:

- passenger ride fallback,
- driver rides and reactive observation,
- create ride draft and deferred publish,
- active ride management deferred actions,
- shared remote operations and helper functions.

This makes the repository a strong demonstration artifact for architecture review and viva voce defense.

---

# 8. Offline Synchronization Lifecycle

## 8.1 Optimistic intent capture

When offline, the system favors capturing user intent immediately rather than rejecting the action.

This is seen in:

- pending ride publishes,
- pending ride actions.

The UI can then reflect that the action is pending synchronization instead of pretending nothing happened.

## 8.2 Pending actions and states

A pending record effectively means:

- the user decision is locally acknowledged,
- the backend has not yet confirmed it,
- the app must preserve and reconcile it later.

This is stronger than a passive cache because it represents mutable domain intent.

## 8.3 Synchronization trigger

Synchronization is triggered by connectivity restoration and repository-level coordination.

This design avoids blind repeated retries while offline and instead performs controlled replay when the runtime environment is capable of success.

## 8.4 Retry flow

The retry flow is pragmatic rather than infinitely recursive.

A typical cycle is:

1. local queue exists,
2. synchronization starts,
3. queued record is attempted,
4. success removes it,
5. failure marks the record with error metadata,
6. future synchronization attempts can retry later.

This keeps failures inspectable and avoids silently losing pending intent.


