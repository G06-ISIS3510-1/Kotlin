# Kotlin Android Project - Comprehensive Dependency Analysis

**Project:** Wheels - Ride-sharing Android App  
**Analysis Date:** 2026-03-16  
**Total Files:** 62 Kotlin files  
**Architecture:** Clean Architecture with MVVM Pattern

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [File Inventory by Layer](#file-inventory-by-layer)
4. [Detailed File Analysis](#detailed-file-analysis)
5. [Dependency Map](#dependency-map)
6. [Critical Dependencies](#critical-dependencies)
7. [Refactoring Recommendations](#refactoring-recommendations)

---

## Executive Summary

The Wheels project is a well-structured Android application following Clean Architecture principles with:
- **Presentation Layer:** MVVM pattern with Jetpack Compose
- **Domain Layer:** Use cases and repository interfaces
- **Data Layer:** Repository implementations with remote APIs
- **Cross-cutting:** Dependency injection with Hilt

### Key Statistics
- **Package Namespaces:** 13 distinct packages
- **ViewModels:** 4 (Home, Rides, Profile, Payments)
- **Screens:** 4 (Home, Rides, Profile, Payments)
- **Domain Models:** 5 (User, Ride, Booking, Payment, Reputation)
- **Repositories:** 3 interfaces + 3 implementations
- **Use Cases:** 3
- **API Interfaces:** 3 (Auth, Ride, Payment)
- **Components:** 4 custom Compose components
- **DI Modules:** 3

---

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│         Presentation Layer (UI)                 │
│  ┌────────────────────────────────────────────┐ │
│  │ Activities   │ ViewModels │ Screens │ Events│ │
│  │ Components   │ UiState    │ Theme   │      │ │
│  └────────────────────────────────────────────┘ │
│                      ↓↑                          │
├─────────────────────────────────────────────────┤
│         Domain Layer (Business Logic)           │
│  ┌────────────────────────────────────────────┐ │
│  │ Use Cases  │ Repositories │ Domain Models  │ │
│  └────────────────────────────────────────────┘ │
│                      ↓↑                          │
├─────────────────────────────────────────────────┤
│         Data Layer (Repositories)               │
│  ┌────────────────────────────────────────────┐ │
│  │ Repository Impl │ Remote │ Local │ Mappers│ │
│  └────────────────────────────────────────────┘ │
│                      ↓↑                          │
├─────────────────────────────────────────────────┤
│  DI (Hilt) │ Navigation │ Common/Utils          │
└─────────────────────────────────────────────────┘
```

---

## File Inventory by Layer

### Application Entry Point (2 files)
- `WheelsApplication.kt` - App initialization with Hilt
- `MainActivity.kt` - Main activity hosting Compose UI

### Navigation (3 files)
- `WheelsNavGraph.kt` - Main navigation graph
- `Destinations.kt` - Navigation destinations (sealed class)
- `BottomNavItem.kt` - Bottom bar navigation model

### Presentation Layer - ViewModels (4 files)
- `ui/screens/home/HomeViewModel.kt`
- `ui/screens/rides/RidesViewModel.kt`
- `ui/screens/profile/ProfileViewModel.kt`
- `ui/screens/payments/PaymentsViewModel.kt`

### Presentation Layer - Screens (4 files)
- `ui/screens/home/HomeScreen.kt`
- `ui/screens/rides/RidesScreen.kt`
- `ui/screens/profile/ProfileScreen.kt`
- `ui/screens/payments/PaymentsScreen.kt`

### Presentation Layer - UI State (4 files)
- `ui/screens/home/HomeUiState.kt` - Contains HomeUiState, HomeQuickStat, ActiveRideUiModel, HomeUpdateUiModel, UpdateTone
- `ui/screens/rides/RidesUiState.kt`
- `ui/screens/profile/ProfileUiState.kt`
- `ui/screens/payments/PaymentsUiState.kt`

### Presentation Layer - UI Events (4 files)
- `ui/screens/home/HomeEvent.kt` - Refresh event
- `ui/screens/rides/RidesEvent.kt` - LoadRides event
- `ui/screens/profile/ProfileEvent.kt` - LoadProfile event
- `ui/screens/payments/PaymentsEvent.kt` - LoadPayments event

### Presentation Layer - Components (4 files)
- `ui/components/WheelsTopBar.kt`
- `ui/components/WheelsBottomBar.kt`
- `ui/components/WheelsCard.kt`
- `ui/components/PrimaryButton.kt`

### Theme & Style (4 files)
- `ui/theme/Type.kt` - Typography configuration
- `ui/theme/Theme.kt` - Main theme composable
- `ui/theme/Shape.kt` - Shape configuration
- `ui/theme/Color.kt` - Color palette (11 colors)

### Domain Layer - Models (5 files)
- `domain/model/User.kt`
- `domain/model/Ride.kt`
- `domain/model/Booking.kt`
- `domain/model/Payment.kt`
- `domain/model/Reputation.kt`

### Domain Layer - Repositories (3 files)
- `domain/repository/AuthRepository.kt` - Interface: getCurrentUser()
- `domain/repository/RideRepository.kt` - Interface: getAvailableRides(), bookRide()
- `domain/repository/PaymentRepository.kt` - Interface: getPayments()

### Domain Layer - Use Cases (3 files)
- `domain/usecase/GetUserProfileUseCase.kt`
- `domain/usecase/GetAvailableRidesUseCase.kt`
- `domain/usecase/BookRideUseCase.kt`

### Data Layer - Repository Implementations (3 files)
- `data/repository/AuthRepositoryImpl.kt`
- `data/repository/RideRepositoryImpl.kt`
- `data/repository/PaymentRepositoryImpl.kt`

### Data Layer - Remote (APIs, DTOs, Mappers) (9 files)
**APIs:**
- `data/remote/api/AuthApi.kt`
- `data/remote/api/RideApi.kt`
- `data/remote/api/PaymentApi.kt`

**DTOs:**
- `data/remote/dto/UserDto.kt`
- `data/remote/dto/RideDto.kt`
- `data/remote/dto/PaymentDto.kt`

**Mappers:**
- `data/remote/mapper/UserMapper.kt` - Extension: UserDto.toDomain()
- `data/remote/mapper/RideMapper.kt` - Extension: RideDto.toDomain()
- `data/remote/mapper/PaymentMapper.kt` - Extension: PaymentDto.toDomain()

### Data Layer - Local (Database, DAOs, Entities, Mappers) (4 files)
- `data/local/database/WheelsDatabase.kt`
- `data/local/dao/BookingDao.kt`
- `data/local/entity/BookingEntity.kt`
- `data/local/mapper/LocalMappers.kt`

### Dependency Injection (3 files)
- `di/AppModule.kt` - Provides CoroutineDispatcher
- `di/NetworkModule.kt` - Provides Retrofit, OkHttpClient, API services
- `di/RepositoryModule.kt` - Binds repository implementations

### Common/Utilities (3 files)
- `common/UiText.kt` - Sealed class for UI text handling
- `common/Constants.kt` - BASE_URL = "https://api.wheels.example/"
- `common/Resource.kt` - Sealed class for async result handling (Success, Error, Loading)

---

## Detailed File Analysis

### WheelsApplication.kt
```
Package: com.wheels.app
Type: Application Entry Point
Extends: Application
Annotations: @HiltAndroidApp

Imports:
  External:
    - android.app.Application
    - dagger.hilt.android.HiltAndroidApp
  Internal: None

Dependents: AndroidManifest.xml
Dependencies: None
```

### MainActivity.kt
```
Package: com.wheels.app
Type: Activity
Extends: ComponentActivity
Annotations: @AndroidEntryPoint

Imports:
  External:
    - android.os.Bundle
    - androidx.activity.ComponentActivity
    - androidx.activity.compose.setContent
    - dagger.hilt.android.AndroidEntryPoint
  Internal:
    - com.wheels.app.navigation.WheelsNavGraph
    - com.wheels.app.ui.theme.WheelsTheme

Dependents: AndroidManifest.xml
Dependencies:
  - navigation/WheelsNavGraph.kt ← Critical routing
  - ui/theme/Theme.kt ← Theme application
```

### Navigation/WheelsNavGraph.kt
```
Package: com.wheels.app.navigation
Type: Navigation Composable
Annotations: @Composable

Key Classes: WheelsNavGraph()

Imports:
  External:
    - androidx.navigation.* (compose helpers)
    - androidx.hilt.navigation.compose.hiltViewModel
  Internal:
    - All 4 Screen ViewModels (HomeViewModel, RidesViewModel, ProfileViewModel, PaymentsViewModel)
    - All 4 Screens (HomeScreen, RidesScreen, ProfileScreen, PaymentsScreen)
    - navigation/Destinations.kt
    - navigation/BottomNavItem.kt
    - ui/components/WheelsBottomBar.kt

Dependents: MainActivity.kt
Dependencies: 11 files ← HUB of navigation
```

### Domain Models
```
User.kt
├─ id: String
├─ fullName: String
├─ email: String
├─ universityId: String
├─ rating: Double
├─ ridesCompleted: Int
└─ isDriver: Boolean

Ride.kt
├─ id: String
├─ driverId: String
├─ origin: String
├─ destination: String
├─ departureTime: Instant
├─ availableSeats: Int
└─ pricePerSeat: Double

Booking.kt
├─ id: String
├─ rideId: String
├─ passengerId: String
├─ seatsReserved: Int
└─ status: String

Payment.kt
├─ id: String
├─ bookingId: String
├─ amount: Double
├─ currency: String
└─ status: String

Reputation.kt
├─ userId: String
├─ score: Double
├─ reviewsCount: Int
└─ badges: List<String>
```

### Use Cases
```
GetUserProfileUseCase
├─ Constructor: @Inject AuthRepository
├─ Method: invoke(): Flow<User?>
└─ Dependents: ProfileViewModel, PaymentsViewModel

GetAvailableRidesUseCase
├─ Constructor: @Inject RideRepository
├─ Method: invoke(): Flow<List<Ride>>
└─ Dependents: RidesViewModel

BookRideUseCase
├─ Constructor: @Inject RideRepository
├─ Method: suspend invoke(rideId, seats): Booking
└─ Dependents: None (not used yet)
```

### ViewModels and Screens
```
HomeViewModel (HomeUiState, HomeEvent)
├─ Dependencies: None (no use cases used)
├─ Provides: uiState: StateFlow<HomeUiState>
└─ Events: Refresh

RidesViewModel (RidesUiState, RidesEvent)
├─ Dependencies: GetAvailableRidesUseCase
├─ Provides: uiState: StateFlow<RidesUiState>
└─ Events: LoadRides

ProfileViewModel (ProfileUiState, ProfileEvent)
├─ Dependencies: GetUserProfileUseCase
├─ Provides: uiState: StateFlow<ProfileUiState>
└─ Events: LoadProfile

PaymentsViewModel (PaymentsUiState, PaymentsEvent)
├─ Dependencies: GetUserProfileUseCase
├─ Provides: uiState: StateFlow<PaymentsUiState>
└─ Events: LoadPayments
```

### Repository Implementations
```
AuthRepositoryImpl
├─ Implements: AuthRepository
├─ getCurrentUser(): Flow<User?> → Returns mock user
└─ Dependencies: None

RideRepositoryImpl
├─ Implements: RideRepository
├─ getAvailableRides(): Flow<List<Ride>> → Returns mock rides
├─ bookRide(rideId, seats): Booking → Returns mock booking
└─ Dependencies: None

PaymentRepositoryImpl
├─ Implements: PaymentRepository
├─ getPayments(): Flow<List<Payment>> → Returns mock payments
└─ Dependencies: None
```

### API Interfaces
```
AuthApi
├─ getCurrentUser(): UserDto [GET /auth/me]
└─ Provides: UserDto

RideApi
├─ getAvailableRides(): List<RideDto> [GET /rides/available]
└─ Provides: List<RideDto>

PaymentApi
├─ getPayments(): List<PaymentDto> [GET /payments]
└─ Provides: List<PaymentDto>
```

### Mappers
```
UserMapper.kt
├─ Function: UserDto.toDomain(): User
├─ Maps: UserDto → domain User
└─ Status: Defined, not actively used

RideMapper.kt
├─ Function: RideDto.toDomain(): Ride
├─ Maps: RideDto → domain Ride, converts departureTimeIso to Instant
└─ Status: Defined, not actively used

PaymentMapper.kt
├─ Function: PaymentDto.toDomain(): Payment
├─ Maps: PaymentDto → domain Payment
└─ Status: Defined, not actively used
```

---

## Dependency Map

### Dependency Layers Visualization

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│ ┌────────────────────────────────────────────────────────┐  │
│ │  MainActivity → WheelsNavGraph → All Screens & VMs    │  │
│ │  ↓                                                      │  │
│ │  HomeScreen ← HomeViewModel ← no use cases            │  │
│ │  RidesScreen ← RidesViewModel ← GetAvailableRidesUC  │  │
│ │  ProfileScreen ← ProfileViewModel ← GetUserProfileUC │  │
│ │  PaymentsScreen ← PaymentsViewModel ← GetUserProfileUC│  │
│ └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                              │
│ ┌────────────────────────────────────────────────────────┐  │
│ │  GetUserProfileUseCase ← AuthRepository              │  │
│ │  GetAvailableRidesUseCase ← RideRepository           │  │
│ │  BookRideUseCase ← RideRepository                    │  │
│ │  5 Domain Models (User, Ride, Booking, Payment, Rep) │  │
│ └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                │
│ ┌────────────────────────────────────────────────────────┐  │
│ │  AuthRepositoryImpl ← AuthApi & DTOs                  │  │
│ │  RideRepositoryImpl ← RideApi & DTOs                  │  │
│ │  PaymentRepositoryImpl ← PaymentApi & DTOs            │  │
│ │  Mappers: UserMapper, RideMapper, PaymentMapper      │  │
│ │  Local: WheelsDatabase, BookingDao, BookingEntity    │  │
│ └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓↑
┌─────────────────────────────────────────────────────────────┐
│        DI (Hilt) | Navigation | Common/Utils               │
│  AppModule, NetworkModule, RepositoryModule, Constants, etc │
└─────────────────────────────────────────────────────────────┘
```

### Direct Dependencies (Critical Path)

#### Path 1: Home Screen Flow
```
MainActivity
  ↓
WheelsNavGraph
  ↓
HomeScreen ← HomeViewModel
  ↓
HomeUiState (no use cases - mock data only)
```

#### Path 2: Rides Screen Flow
```
MainActivity
  ↓
WheelsNavGraph
  ↓
RidesScreen ← RidesViewModel
  ↓
GetAvailableRidesUseCase (injected)
  ↓
RideRepository (interface)
  ↓
RideRepositoryImpl (via RepositoryModule binding)
  ↓
RideApi + Mappers
  ↓
Retrofit + OkHttpClient (from NetworkModule)
```

#### Path 3: Profile Screen Flow
```
MainActivity
  ↓
WheelsNavGraph
  ↓
ProfileScreen ← ProfileViewModel
  ↓
GetUserProfileUseCase (injected)
  ↓
AuthRepository (interface)
  ↓
AuthRepositoryImpl (via RepositoryModule binding)
  ↓
AuthApi + UserMapper
  ↓
Retrofit + OkHttpClient
```

#### Path 4: Payments Screen Flow
```
MainActivity
  ↓
WheelsNavGraph
  ↓
PaymentsScreen ← PaymentsViewModel
  ↓
GetUserProfileUseCase (injected) ← Uses AuthRepository
  ↓
No PaymentRepository connection yet (incomplete)
```

---

## Critical Dependencies

### Must-Have References (High Impact)

| From | To | Type | Impact |
|------|-----|------|--------|
| MainActivity.kt | WheelsNavGraph.kt | Direct | App boot sequence |
| WheelsNavGraph.kt | All Screen ViewModels | Direct | Navigation structure |
| RidesViewModel.kt | GetAvailableRidesUseCase.kt | Inject | Screen functionality |
| ProfileViewModel.kt | GetUserProfileUseCase.kt | Inject | Screen functionality |
| GetUserProfileUseCase.kt | AuthRepository.kt | Inject | Business logic |
| GetAvailableRidesUseCase.kt | RideRepository.kt | Inject | Business logic |
| RepositoryModule.kt | Repository Implementations | Binding | DI setup |
| NetworkModule.kt | Retrofit, OkHttp, APIs | Provide | Network config |
| RideRepositoryImpl.kt | RideApi.kt | Usage | Data fetching |
| Theme.kt | Color.kt, Shape.kt, Type.kt | Direct | Theme composition |

### Circular Dependencies: ✅ NONE DETECTED

### Unused Classes: ⚠️ POTENTIAL IMPROVEMENTS NEEDED

- `BookingEntity.kt` - Defined but not connected to database
- `BookingDao.kt` - Defined but not implemented
- `WheelsDatabase.kt` - Declared but not configured
- `LocalMappers.kt` - Empty object, no mappers defined
- `BookRideUseCase.kt` - Defined but not used anywhere
- `Reputation.kt` - Model defined but not used
- `WheelsTopBar.kt` - Defined but not used by screens
- `UserMapper.kt` - Extension function defined but APIs not connected
- `RideMapper.kt` - Extension function defined but APIs not connected
- `PaymentMapper.kt` - Extension function defined but APIs not connected

---

## Refactoring Recommendations

### Priority 1: Complete the Data Layer

**Issue:** Repository implementations return mock data  
**Action:**
```kotlin
// RideRepositoryImpl should use RideApi
class RideRepositoryImpl @Inject constructor(
    private val rideApi: RideApi
) : RideRepository {
    override fun getAvailableRides(): Flow<List<Ride>> = flow {
        emit(rideApi.getAvailableRides().map { it.toDomain() })
    }.catch { ... }
}
```

### Priority 2: Wire Up Local Database

**Issue:** Room database components exist but aren't connected  
**Action:**
```kotlin
@Database(entities = [BookingEntity::class], version = 1)
abstract class WheelsDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}

// Implement BookingDao
@Dao
interface BookingDao {
    @Insert suspend fun insertBooking(booking: BookingEntity)
    @Query("SELECT * FROM bookings") fun getAllBookings(): Flow<List<BookingEntity>>
}
```

### Priority 3: Connect Repositories to APIs

**Issue:** Repositories don't inject APIs or mappers  
**Action:**
```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {
    override fun getCurrentUser(): Flow<User?> = flow {
        emit(authApi.getCurrentUser().toDomain())
    }
}
```

### Priority 4: Improve Payments Flow

**Issue:** PaymentsViewModel uses GetUserProfileUseCase instead of payment-specific logic  
**Suggestion:**
- Create `GetPaymentsUseCase`
- Wire PaymentsViewModel to it
- Implement PaymentRepository properly

### Priority 5: Add Error Handling

**Issue:** Use cases don't handle exceptions from API calls  
**Suggestion:**
```kotlin
override fun invoke(): Flow<List<Ride>> = rideRepository.getAvailableRides()
    .catch { exception ->
        // Emit error state or log
    }
    .onStart { 
        // Emit loading state
    }
```

### Priority 6: Create Shared Base Classes

**Pattern:**
```kotlin
abstract class BaseViewModel<State, Event> : ViewModel() {
    protected val _uiState = MutableStateFlow<State>(...)
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    
    abstract fun onEvent(event: Event)
}
```

**Benefit:** Reduce boilerplate in all ViewModels

### Priority 7: Complete HomeViewModel

**Issue:** HomeViewModel has rich UiState but doesn't use any use cases  
**Suggestion:**
- Connect HomeViewModel to relevant use cases
- Fetch initial data in init block
- Handle real user profile and ride data

### Priority 8: Remove Unused Code

**Files to Review/Remove:**
- `BookRideUseCase.kt` - Not used anywhere (unless planned)
- `Reputation.kt` - No current use
- `LocalMappers.kt` - Empty
- `WheelsTopBar.kt` - Not used in screens

---

## Package Structure Summary

```
com.wheels.app/
├── WheelsApplication.kt
├── MainActivity.kt
├── di/
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── navigation/
│   ├── Destinations.kt
│   ├── BottomNavItem.kt
│   └── WheelsNavGraph.kt
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Ride.kt
│   │   ├── Booking.kt
│   │   ├── Payment.kt
│   │   └── Reputation.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── RideRepository.kt
│   │   └── PaymentRepository.kt
│   └── usecase/
│       ├── GetUserProfileUseCase.kt
│       ├── GetAvailableRidesUseCase.kt
│       └── BookRideUseCase.kt
├── data/
│   ├── repository/
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── RideRepositoryImpl.kt
│   │   └── PaymentRepositoryImpl.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApi.kt
│   │   │   ├── RideApi.kt
│   │   │   └── PaymentApi.kt
│   │   ├── dto/
│   │   │   ├── UserDto.kt
│   │   │   ├── RideDto.kt
│   │   │   └── PaymentDto.kt
│   │   └── mapper/
│   │       ├── UserMapper.kt
│   │       ├── RideMapper.kt
│   │       └── PaymentMapper.kt
│   └── local/
│       ├── database/
│       │   └── WheelsDatabase.kt
│       ├── dao/
│       │   └── BookingDao.kt
│       ├── entity/
│       │   └── BookingEntity.kt
│       └── mapper/
│           └── LocalMappers.kt
├── ui/
│   ├── theme/
│   │   ├── Type.kt
│   │   ├── Theme.kt
│   │   ├── Shape.kt
│   │   └── Color.kt
│   ├── components/
│   │   ├── WheelsTopBar.kt
│   │   ├── WheelsBottomBar.kt
│   │   ├── WheelsCard.kt
│   │   └── PrimaryButton.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeViewModel.kt
│       │   ├── HomeScreen.kt
│       │   ├── HomeUiState.kt
│       │   └── HomeEvent.kt
│       ├── rides/
│       │   ├── RidesViewModel.kt
│       │   ├── RidesScreen.kt
│       │   ├── RidesUiState.kt
│       │   └── RidesEvent.kt
│       ├── profile/
│       │   ├── ProfileViewModel.kt
│       │   ├── ProfileScreen.kt
│       │   ├── ProfileUiState.kt
│       │   └── ProfileEvent.kt
│       └── payments/
│           ├── PaymentsViewModel.kt
│           ├── PaymentsScreen.kt
│           ├── PaymentsUiState.kt
│           └── PaymentsEvent.kt
└── common/
    ├── UiText.kt
    ├── Constants.kt
    └── Resource.kt
```

---

## External Dependencies Used

### Android Framework
- `android.app.Application`
- `android.os.Bundle`
- `androidx.activity.ComponentActivity`
- `androidx.activity.compose.setContent`
- `androidx.lifecycle.ViewModel`

### Jetpack Compose
- `androidx.compose.foundation.*`
- `androidx.compose.material.icons.*`
- `androidx.compose.material3.*`
- `androidx.compose.runtime.*`
- `androidx.compose.ui.*`

### Navigation
- `androidx.navigation.compose.*`
- `androidx.hilt.navigation.compose.hiltViewModel`

### Dependency Injection
- `dagger.hilt.*`
- `javax.inject.Inject`

### Networking
- `retrofit2.*`
- `okhttp3.OkHttpClient`
- `retrofit2.converter.gson.GsonConverterFactory`

### Coroutines
- `kotlinx.coroutines.*`

### Standard Library
- `java.time.Instant`

---

## Next Steps for Refactoring

1. **Connect all repository implementations to actual APIs**
2. **Implement Room database with proper entities and DAOs**
3. **Add error handling and loading states to use cases**
4. **Create base classes to reduce boilerplate**
5. **Wire up PaymentsViewModel to PaymentRepository**
6. **Complete HomeViewModel with real data loading**
7. **Implement caching strategy in repositories**
8. **Add unit tests for use cases and repositories**
9. **Remove unused code (BookRideUseCase, Reputation, etc.)**
10. **Document API contracts and error handling**

---

## Conclusion

The Wheels project has a solid foundation with proper Clean Architecture implementation. The main areas for improvement are:
- Connecting mock data repositories to real APIs
- Implementing the local database layer
- Adding proper error handling and loading states
- Reducing boilerplate code with base classes
- Completing incomplete features (Payments, Bookings)

This dependency map should serve as a reference for understanding the project structure and planning refactoring efforts.
