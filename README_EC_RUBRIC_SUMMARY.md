# Wheels EC Rubric Summary

## Scope
This document summarizes the implemented **Eventual Connectivity (EC)** scenarios in Wheels and maps each one to the supporting **multi-threading**, **local storage**, and **caching** strategies used in the current Kotlin + Jetpack Compose + MVVM + Repository + Firebase architecture.

The goal is to make the implementation easy to defend against the course rubric.

---

## Rubric Reference

### Eventual Connectivity
- **5 points per protected view / real EC scenario**
- **Maximum: 20 points**
- The rubric expects real offline behavior, navigation continuity, or fallback behavior. Generic "No internet" messages do not count.

### Multi-threading
- **Coroutine with a dispatcher**: 5 points
- **Multiple coroutines nested / coordinated with Input/Output**: 10 points
- **One Input/Output and one Main**: 10 points
- **Maximum: 20 points**

### Local Storage
- **Relational local database**: 10 points
- **Key-value DB (Hive / RealmDB style)**: 5 points
- **Local files**: 5 points
- **Preferences / DataStore / SharedPreferences / KeyChain**: 5 points
- **Maximum: 20 points**

### Caching
- **Image caching libraries**: 5 points
- **LRU / SparseArray / ArrayMap / NSCache**: 10 points
- **Maximum: 20 points**

---

## Scenario Summary Table

| EC Scenario | Protected View / Flow | Eventual Connectivity Strategy | Multi-threading Used | Local Storage Used | Caching Used | Expected Rubric Points |
|---|---|---|---|---|---|---|
| 1. Offline session continuity | Auth/session restore + protected navigation | Restore the last valid session offline instead of blocking the app. If the device has no internet, the user can still reopen the app and continue with the restored session snapshot. | `viewModelScope`, reactive auth observation, repository I/O work | `DataStore` persists the last session snapshot | None required | EC: **5** + Local Storage (`DataStore`): **5** + Multi-threading: **5-10** |
| 2. Create Ride offline draft + deferred publish | `Create Ride` | Draft is saved locally while the user types. If the user publishes offline, the ride is queued as `Pending to Publish` and synced automatically when internet returns. | `viewModelScope.launch`, repository `withContext(ioDispatcher)`, reactive sync when connectivity returns | `Room` for draft persistence and pending ride publishes | None required for the critical write path | EC: **5** + Local Storage (`Room`): **10** + Multi-threading: **15-20** |
| 3. Active Ride Management offline actions | `Active Ride Management` | `Start`, `End`, `Cancel`, and `Delete` can be queued offline. The UI reflects a pending action immediately and the backend mutation is replayed later. | Multiple coroutines coordinated across `ViewModel`, `Room`, Firebase, and trust score sync; Main + I/O split is explicit | `Room` for pending ride actions | None required for the critical write path | EC: **5** + Local Storage (`Room`): **10** + Multi-threading: **15-20** |
| 4. Find / Search Rides offline fallback | Passenger `Rides` search/list | If rides were already loaded from Firebase, the last available list is served from cache when internet drops. Filters still work offline over the cached list. | Flow collection in `ViewModel`, repository work with I/O checks and cache fallback | No persistent local DB required for this scenario | `LruCache` for available rides list | EC: **5** + Caching (`LruCache`): **10** + Multi-threading: **5-10** |
| 5. My Rides offline fallback in-session | Driver `My Rides` list | If the driver already loaded rides from Firebase, the last list remains available from cache when connectivity drops during the session. This complements the Room-based pending publish / pending action system. | Flow collection in `ViewModel`, repository network check + cache fallback, Main + I/O coordination | `Room` still supports pending drafts/actions, but list fallback itself is memory cache | `LruCache` for driver rides list | Caching (`LruCache`): **10** support evidence; useful reinforcement for EC story |

---

## Expected Totals We Can Defend

### Eventual Connectivity
The strongest 4 scenarios to present are:
1. Offline session continuity
2. Create Ride offline draft + deferred publish
3. Active Ride Management offline actions
4. Find / Search Rides offline fallback

These give:
- **4 x 5 = 20 / 20** in Eventual Connectivity

### Local Storage
The strongest implemented items are:
- `Room` relational local storage for drafts, pending publishes, and pending ride actions: **10 points**
- `DataStore` for session persistence: **5 points**

Defendable local storage total:
- **15 / 20**

### Caching
The strongest implemented item is:
- `LruCache` for available passenger rides and driver `My Rides`: **10 points**

Defendable caching total:
- **10 / 20**

### Multi-threading
The strongest implemented evidence is in:
- `Create Ride offline draft + deferred publish`
- `Active Ride Management offline actions`

Defendable multi-threading score:
- **At least 15 / 20**, and arguably **20 / 20** if explained well

Why:
- Coroutines with dispatcher are explicit
- Main/UI orchestration and I/O work are split clearly
- Multiple coroutines and reactive flows are coordinated across ViewModel, Room, Firebase, and connectivity observation

---

## Strategy Details By Technology

## 1. DataStore
### Where it is used
- Offline session continuity

### What it stores
- Last valid session snapshot
- User identity and active role required to restore the session locally

### Why it fits
This is lightweight state, not relational data. It should survive app restarts and support protected navigation even when Firebase cannot refresh because the device is offline.

### Main files
- [AuthSessionLocalStore.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/auth/data/local/AuthSessionLocalStore.kt)
- [AuthRepositoryImpl.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/auth/data/repository/AuthRepositoryImpl.kt)
- [AuthSessionViewModel.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/auth/presentation/session/AuthSessionViewModel.kt)
- [WheelsNavGraph.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/core/navigation/WheelsNavGraph.kt)

### Rubric contribution
- Preferences / DataStore: **5 points**

---

## 2. Room
### Where it is used
- Create Ride offline draft persistence
- Pending ride publishes
- Pending ride actions in Active Ride Management

### What it stores
- `create_ride_drafts`
- `pending_ride_publishes`
- `pending_ride_actions`

### Why it fits
This is structured, relational, durable local state. These flows must survive app restarts and support deferred synchronization, so in-memory caching would not be enough.

### Main files
- [WheelsDatabase.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/core/database/WheelsDatabase.kt)
- [DatabaseModule.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/core/di/DatabaseModule.kt)
- [RideOfflineDao.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/RideOfflineDao.kt)
- [CreateRideDraftEntity.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/CreateRideDraftEntity.kt)
- [PendingRidePublishEntity.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/PendingRidePublishEntity.kt)
- [PendingRideActionEntity.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/PendingRideActionEntity.kt)
- [RideRepositoryImpl.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/repository/RideRepositoryImpl.kt)

### Rubric contribution
- Relational local database: **10 points**

---

## 3. LruCache
### Where it is used
- Passenger available rides / search fallback
- Driver `My Rides` list fallback during the current app session

### What it stores
- Last available rides list fetched from Firebase
- Last driver rides list fetched from Firebase

### Why it fits
This is a classic hot-data cache. The data is read frequently, should be fast to reuse, and is acceptable to lose when the process is killed because Firebase remains the remote source of truth.

This is not a substitute for Room. It is a **memory cache** that improves offline continuity during the current process lifetime.

### Main files
- [AvailableRidesLocalCache.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/AvailableRidesLocalCache.kt)
- [DriverRidesLocalCache.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/DriverRidesLocalCache.kt)
- [NearRidesLocalCache.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/local/NearRidesLocalCache.kt)
- [RideRepositoryImpl.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/repository/RideRepositoryImpl.kt)

### Rubric contribution
- LRU / in-memory cache structure: **10 points**

### Implementation decisions to explain
- `MAX_ENTRIES` bounds memory usage
- cache keys are intentionally small and deterministic
- Firebase remains the source of truth
- cache is used as fallback only when network is unavailable or unstable

---

## 4. Multi-threading with Coroutines
### Where it is strongest
- Create Ride offline draft + deferred publish
- Active Ride Management offline actions

### Why it is strong
The implementation does not block Compose/UI while performing:
- Room reads/writes
- Firestore writes
- trust score synchronization
- connectivity-driven replay of pending commands

The architecture clearly separates:
- UI orchestration on `Main` through `viewModelScope`
- I/O work on `Dispatchers.IO` through repository methods and `withContext(ioDispatcher)`

### Main files
- [RidesViewModel.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/presentation/viewmodel/RidesViewModel.kt)
- [RideRepositoryImpl.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/features/rides/data/repository/RideRepositoryImpl.kt)
- [FirebaseDriverTrustRepository.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/core/trust/data/repository/FirebaseDriverTrustRepository.kt)
- [AndroidNetworkMonitor.kt](/Users/aneira3/Documents/Kotlin/app/src/main/java/com/wheels/app/core/network/AndroidNetworkMonitor.kt)

### Rubric contribution
#### Safely defendable
- Coroutine with a dispatcher: **5 points**
- One Main + one Input/Output: **10 points**

#### Strongly defendable if explained well
- Multiple coroutines coordinated with Input/Output: **10 points**

### The best explanation
- `viewModelScope.launch { ... }` starts screen-bound orchestration
- repositories move disk/network work to `ioDispatcher`
- offline queues are replayed reactively when connectivity returns
- trust score sync chains multiple async layers without freezing the UI

---

## Recommended Oral Defense

### Eventual Connectivity
> We implemented four real eventual connectivity scenarios with actual offline behavior, not generic error messages: session continuity, create ride draft and deferred publish, active ride offline actions, and rides list fallback from cache.

### Local Storage
> We used DataStore for lightweight persisted session state and Room for structured durable offline state such as drafts, pending ride publishes, and pending ride actions.

### Caching
> We used LruCache for hot Firebase-backed lists such as available rides and My Rides. This gives fast in-memory fallback during the current session while keeping Firebase as the remote source of truth.

### Multi-threading
> We used Kotlin Coroutines, `viewModelScope`, `Flow`, and `Dispatchers.IO` to separate UI orchestration from Room and Firebase I/O. This is especially visible in Create Ride and Active Ride Management, where offline actions are persisted and replayed asynchronously when connectivity returns.

---

## Final Position

With the current implementation, the strongest defendable outcome is:
- **Eventual Connectivity:** 20 / 20
- **Local Storage:** 15 / 20
- **Caching:** 10 / 20
- **Multi-threading:** 15 to 20 / 20

This is already a strong, coherent, and defensible delivery.
