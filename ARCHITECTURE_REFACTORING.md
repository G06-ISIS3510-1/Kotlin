# Wheels Android App - Clean + Feature-Based Architecture Refactoring

## Overview
This Kotlin Android project has been refactored from a layer-based architecture to a clean + feature-based architecture. This improves scalability, modularity, and makes it easier to develop features independently.

## New Architecture Structure

```
com.wheels.app
│
├── core/                           # Shared infrastructure & UI components
│   ├── common/                     # Utilities
│   │   ├── Constants.kt
│   │   ├── Resource.kt (sealed class for API responses)
│   │   └── UiText.kt
│   │
│   ├── database/                   # Room database
│   │   └── WheelsDatabase.kt
│   │
│   ├── di/                         # Dependency injection
│   │   ├── AppModule.kt
│   │   ├── NetworkModule.kt
│   │   └── RepositoryModule.kt
│   │
│   ├── navigation/                 # App-wide navigation
│   │   ├── Destinations.kt
│   │   ├── BottomNavItem.kt
│   │   └── WheelsNavGraph.kt
│   │
│   └── ui/                         # Reusable UI components & theme
│       ├── components/
│       │   ├── WheelsBottomBar.kt
│       │   ├── WheelsCard.kt
│       │   ├── WheelsTopBar.kt
│       │   └── PrimaryButton.kt
│       │
│       └── theme/
│           ├── Color.kt
│           ├── Shape.kt
│           ├── Theme.kt
│           └── Type.kt
│
├── features/                       # Feature modules (organized by domain)
│   │
│   ├── auth/                       # Authentication feature
│   │   ├── domain/
│   │   │   └── repository/
│   │   │       └── AuthRepository.kt (interface)
│   │   │
│   │   └── data/
│   │       ├── remote/
│   │       │   └── api/
│   │       │       └── AuthApi.kt
│   │       │
│   │       └── repository/
│   │           └── AuthRepositoryImpl.kt
│   │
│   ├── rides/                      # Rides feature
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Ride.kt
│   │   │   │   └── Booking.kt
│   │   │   ├── repository/
│   │   │   │   └── RideRepository.kt
│   │   │   └── usecase/
│   │   │       ├── GetAvailableRidesUseCase.kt
│   │   │       └── BookRideUseCase.kt
│   │   │
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── RideApi.kt
│   │   │   │   ├── dto/
│   │   │   │   │   └── RideDto.kt
│   │   │   │   └── mapper/
│   │   │   │       └── RideMapper.kt
│   │   │   │
│   │   │   └── repository/
│   │   │       └── RideRepositoryImpl.kt
│   │   │
│   │   └── presentation/
│   │       ├── viewmodel/
│   │       │   └── RidesViewModel.kt
│   │       └── ui/
│   │           └── RidesScreen.kt
│   │
│   ├── payments/                   # Payments feature
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── Payment.kt
│   │   │   └── repository/
│   │   │       └── PaymentRepository.kt
│   │   │
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── PaymentApi.kt
│   │   │   │   ├── dto/
│   │   │   │   │   └── PaymentDto.kt
│   │   │   │   └── mapper/
│   │   │   │       └── PaymentMapper.kt
│   │   │   │
│   │   │   └── repository/
│   │   │       └── PaymentRepositoryImpl.kt
│   │   │
│   │   └── presentation/
│   │       ├── viewmodel/
│   │       │   └── PaymentsViewModel.kt
│   │       └── ui/
│   │           └── PaymentsScreen.kt
│   │
│   ├── profile/                    # User Profile feature
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── User.kt
│   │   │   │   └── Reputation.kt
│   │   │   ├── repository/
│   │   │   │   └── AuthRepository.kt
│   │   │   └── usecase/
│   │   │       └── GetUserProfileUseCase.kt
│   │   │
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── AuthApi.kt
│   │   │   │   ├── dto/
│   │   │   │   │   └── UserDto.kt
│   │   │   │   └── mapper/
│   │   │   │       └── UserMapper.kt
│   │   │   │
│   │   │   └── repository/
│   │   │       └── AuthRepositoryImpl.kt (shares with auth feature)
│   │   │
│   │   └── presentation/
│   │       ├── viewmodel/
│   │       │   └── ProfileViewModel.kt
│   │       └── ui/
│   │           └── ProfileScreen.kt
│   │
│   └── home/                       # Home feature
│       └── presentation/
│           ├── viewmodel/
│           │   └── HomeViewModel.kt
│           └── ui/
│               └── HomeScreen.kt
│
├── MainActivity.kt                 # App entry point
└── WheelsApplication.kt            # Hilt app entry point
```

## Key Changes

### 1. **Package Structure**
- **Old:** Organized by layer (domain, data, ui)
- **New:** Organized by feature with domain/data/presentation layers inside each

### 2. **Feature Modules Created**
- `auth` - Authentication repository and APIs
- `rides` - Ride listings and booking management
- `payments` - Payment processing
- `profile` - User profile and reputation
- `home` - Dashboard/home screen

### 3. **Core Module Created**
Shared components moved to `core/`:
- UI theme and components (`ui/theme`, `ui/components`)
- Navigation infrastructure (`navigation/`)
- Dependency injection (`di/`)
- Common utilities (`common/`)
- Database infrastructure (`database/`)

### 4. **Import Updates**
All imports have been updated from old paths to new feature-specific paths:
- `com.wheels.app.ui.theme.*` → `com.wheels.app.core.ui.theme.*`
- `com.wheels.app.ui.components.*` → `com.wheels.app.core.ui.components.*`
- `com.wheels.app.navigation.*` → `com.wheels.app.core.navigation.*`
- `com.wheels.app.di.*` → `com.wheels.app.core.di.*`
- `com.wheels.app.domain.*` → `com.wheels.app.features.{feature}.domain.*`

### 5. **Model Organization**
- Domain models placed with their respective features
- User model is in `profile/domain/model/`
- Ride model is in `rides/domain/model/`
- Payment model is in `payments/domain/model/`




