# Wheels Eventual Connectivity Plan

## Purpose

This document defines the implementation plan for **Eventual Connectivity**, **Local Storage**, and **Caching** in Wheels before coding begins.

The goal is to choose a strategy that is:

- technically coherent with the current architecture
- realistic given the already implemented features
- easy to defend in the viva voce
- strong with respect to the course rubric

This is a **design and planning document**, not a record of completed implementation.

---

## 1. Current Architectural Context

Wheels already uses:

- Kotlin
- Jetpack Compose
- MVVM
- Repository Pattern
- Firebase Auth
- Firestore
- Cloud Functions
- feature-based organization with clean-ish layering
- reactive state through `Flow` / `StateFlow`

Relevant implemented flows we can build on:

- Institutional login
- Session restore
- Create Ride
- Passenger ride search/listing
- Passenger booking flow
- Active Ride Management
- Cancel Ride
- End Ride
- Delete Ride
- Driver Reliability Score
- Q6 ride cancellation analytics
- Sensor-based current location and autocomplete related flows

Important architectural reality:

- **Create Ride** and **Active Ride Management** already perform real backend operations, so they are strong candidates for offline continuity.
- **Search / Find Rides** already works as a read-oriented flow and is a natural candidate for cache fallback.
- **Group Chat** currently remains much more mock/in-memory than the ride and trust flows, so it is not the best primary scenario for a rubric-driven offline strategy.
- **Room** already exists in the project, although it is currently used only for a different analytics flow.
- **LruCache** is already present in the codebase for a nearby-rides-related local cache pattern, so caching is already conceptually aligned with the project.

---

## 2. Rubric Interpretation

### Eventual Connectivity

The rubric requires:

- 4 real protected-view scenarios
- 5 points each
- actual offline behavior, not just error messages
- examples such as:
  - offline navigation
  - fallback to cache
  - local persistence
  - deferred synchronization
  - optimistic update

Therefore, our plan must avoid these anti-patterns:

- blocked app
- “No internet” as the only behavior
- lost content
- forcing the user to retry everything manually
- destructive local behavior with no later reconciliation

### Local Storage

The rubric rewards:

- relational local DB: **10**
- key-value/prefs style storage: **5**

This strongly suggests using:

- **Room** for structured offline state
- **DataStore** for lightweight session/preference state

### Caching

The rubric rewards:

- LRU / ArrayMap / SparseArray style in-memory cache: **10**

This suggests using:

- **LruCache** as the main explicit caching mechanism
- **ArrayMap** only as a complementary lightweight in-memory map if needed

---

## 3. Final Recommended Strategy

We will implement **4 eventual connectivity scenarios** using a combination of:

- **Room**
- **DataStore**
- **LruCache**
- optionally **ArrayMap** as a small complement

We will **not** use Group Chat as one of the 4 primary graded scenarios, because it is currently less mature architecturally and would cost more effort for a weaker defense.

---

## 4. Chosen Eventual Connectivity Scenarios

## Scenario 1. Offline Session Continuity

### View / Flow

- SessionGate
- institutional login session restore
- authenticated navigation restore

### Strategy

**Persistent session continuity with offline access to the last valid authenticated state**

This is not offline authentication. The user is not logging in without internet from scratch. Instead, the app preserves the last valid session state so the app does not become unusable just because connectivity is temporarily unavailable.

### Technical direction

- use **DataStore**
- persist minimal session restore metadata and/or active role preferences
- allow the app to reopen protected navigation with the restored session state when available
- reconcile later when Firebase session observation resumes

### Why this fits the rubric

This is a true offline-protected-view scenario because:

- the user can still access the app flow
- the app is not blocked by connectivity loss
- there is real persistence, not just a message

### Rubric contribution

- Eventual Connectivity: **5**
- Local Storage via DataStore: contributes toward **5**

---

## Scenario 2. Create Ride Offline Draft + Deferred Publish

### View / Flow

- Create Ride screen

### Strategy

**Draft offline, commit online**

The user should be able to prepare a ride even without stable connectivity, and the publish action should not be lost.

### Technical direction

Use **Room** to persist:

- ride draft data
- pending publish commands

Suggested tables:

- `ride_drafts`
- `pending_ride_actions`

Suggested lifecycle:

1. user fills Create Ride form
2. draft is persisted locally
3. if publish happens offline, create a pending command with status `PENDING`
4. UI shows that the ride is pending synchronization
5. once connectivity returns, background sync publishes the ride
6. on success, pending command becomes `SYNCED`
7. Firestore becomes the authoritative source of truth

### Why this fits the rubric

This is one of the strongest offline scenarios because:

- it avoids lost content
- it uses real local persistence
- it supports deferred synchronization
- it provides meaningful offline behavior

### Rubric contribution

- Eventual Connectivity: **5**
- Local Storage via Room: contributes toward **10**

---

## Scenario 3. Active Ride Management Offline Actions

### View / Flow

- Active Ride Management
- Cancel Ride
- End Ride
- Delete Ride

### Strategy

**Offline-first command queue with optimistic update and deferred synchronization**

Critical ride actions must not be lost if the connection drops after the user confirms them.

### Technical direction

Use **Room** for:

- `pending_ride_actions`

Each action record would include:

- local id
- ride id
- action type (`CANCEL`, `END`, `DELETE`)
- status (`PENDING`, `SYNCED`, `FAILED`)
- retry count
- created timestamp

Operational behavior:

1. user confirms action
2. UI updates immediately with a visible sync state
3. action is stored locally
4. background retry eventually syncs to Firestore
5. Cloud Functions then update:
   - trust score
   - trust metrics
   - cancellation analytics

### Why this fits the rubric

This is a very strong eventual connectivity case because it demonstrates:

- optimistic update
- persistent command queue
- deferred synchronization
- backend reconciliation

### Why it fits the architecture

This flow is already real in the app:

- it already writes operational state to Firestore
- it already triggers backend trust and analytics logic

So the offline strategy extends an existing architecture instead of inventing a new one.

### Rubric contribution

- Eventual Connectivity: **5**
- Local Storage via Room: further supports the relational DB criterion

---

## Scenario 4. Find / Search Rides Offline Fallback

### View / Flow

- passenger ride listing
- ride search / filtering

### Strategy

**Network falling back to cache**

If the network is unavailable, the user should still be able to see recently cached ride results instead of a blank screen.

### Technical direction

Use **LruCache** for:

- recently used ride search results
- cached ride result sets by query/filter combination

Possible cache key:

- search text
- selected area
- max price
- min rating

Operational behavior:

1. user opens Find Rides
2. app tries network / Firestore
3. if fresh data is available, update cache
4. if network is unavailable, return cached results
5. if no cache exists, then show an informative fallback state

### Why this fits the rubric

This is a real offline fallback:

- the user can still navigate and see meaningful data
- not just a generic connectivity error
- explicit cache strategy is visible and defendable

### Why LRU is a good fit

Because this flow naturally follows the **locality principle**:

- recent searches are the most likely to be repeated
- old search results are colder data
- LRU provides a clear eviction policy

### Rubric contribution

- Eventual Connectivity: **5**
- Caching via LRU: contributes toward **10**

---

## 5. Technologies We Will Use

## Room

### We will use Room for

- Create Ride drafts
- pending ride actions for:
  - cancel
  - end
  - delete
  - publish

### Why Room

Because these are:

- structured entities
- queryable
- stateful over time
- ideal for relational local persistence

### Rubric value

- **Relational Local Database = 10**

---

## DataStore

### We will use DataStore for

- session continuity metadata
- lightweight role/restore preferences
- small preference-style values

### Why DataStore

Because this is:

- lightweight
- preference-like
- not relational
- strongly aligned with session persistence

### Rubric value

- **Preferences / DataStore = 5**

---

## LruCache

### We will use LruCache for

- passenger ride search results
- recent `Find Rides` query results

### Why LruCache

Because this is:

- hot read-heavy data
- repeated access data
- memory-conscious caching
- easy to explain with locality + eviction policy

### Rubric value

- **Caching (LRU family) = 10**

---

## ArrayMap

### We may use ArrayMap only as a complement

Potential use:

- lightweight in-memory hot map for active ride snapshot or trust score snapshot

### Why it is not a core strategy

Because:

- it is useful as a supporting optimization
- but it is not as strong or as visible as the main LRU caching scenario

So ArrayMap is optional, not central.

---

## SparseArray

### We do not recommend SparseArray

Because our natural keys are mostly:

- `rideId`
- `driverId`
- query keys

Those are string-like identifiers, not integer keys. SparseArray is therefore a weaker fit for the project.

---

## 6. What We Are Not Choosing

## Group Chat as a main offline scenario

We are intentionally not choosing ride group chat as one of the four main graded scenarios.

### Why

Although it is conceptually interesting, the current implementation is still much closer to an in-memory/mock interaction than a backend-driven synchronized chat system.

Implementing offline chat correctly would require:

- message repository
- message persistence
- delivery states
- replay logic
- conflict handling

That is too much implementation cost for weaker rubric clarity compared to the four selected scenarios.

### Conclusion

Chat offline can be mentioned as future work, but it should not be part of the first graded implementation wave.

---

## 7. Final Mapping to the Rubric

### Eventual Connectivity

Chosen 4 scenarios:

1. Offline session continuity
2. Create Ride offline draft + deferred publish
3. Active Ride Management offline actions
4. Find / Search Rides cache fallback

Expected Eventual Connectivity coverage:

- 4 protected-view scenarios
- 5 points each
- **Target: 20 / 20**

### Local Storage

Chosen technologies:

- Room
- DataStore

Expected Local Storage coverage:

- Room relational DB: **10**
- DataStore / preferences: **5**
- **Target: 15 / 15** in the categories we plan to use

### Caching

Chosen technologies:

- LruCache
- optional ArrayMap as complement

Expected Caching coverage:

- LRU-family in-memory cache: **10**
- **Target: 10 / 10**

---

## 8. Why This Plan Is Easy to Defend Orally

This plan is strong in a viva because each scenario maps cleanly to course concepts:

| Scenario | Main Concept |
|---|---|
| Session continuity | persistent state, offline continuity |
| Create Ride | draft offline, commit online |
| Active Ride Management | deferred synchronization, optimistic update, command queue |
| Find / Search Rides | cache then network, network fallback to cache, LRU eviction |

It also aligns with the current architecture:

- MVVM
- repositories
- reactive flows
- backend as source of truth
- Cloud Functions for business side effects

This makes the proposal not only technically valid, but also clearly connected to the codebase that already exists.

---

## 9. Final Recommendation

We should implement the following:

### Eventual Connectivity

- offline session continuity
- Create Ride draft + pending publish
- Active Ride Management pending command queue
- Find Rides cache fallback

### Local Storage

- Room for drafts and pending ride commands
- DataStore for session/role lightweight persistence

### Caching

- LruCache for ride search results
- optional ArrayMap only if a small in-memory helper is useful

### We should not prioritize

- full offline chat
- SparseArray
- Room for everything
- overlapping caches that conflict with teammates' ownership

---

## 10. Summary

The best balance of:

- effort
- rubric points
- architectural clarity
- oral defense quality

is:

1. **Session continuity with DataStore**
2. **Create Ride offline persistence with Room**
3. **Active Ride Management deferred sync with Room**
4. **Find Rides offline fallback with LruCache**

This plan maximizes rubric value while staying consistent with the real state of the Wheels codebase.
