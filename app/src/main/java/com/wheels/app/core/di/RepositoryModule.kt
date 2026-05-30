package com.wheels.app.core.di

import com.wheels.app.core.analytics.data.repository.FirebaseUserDestinationInsightsRepository
import com.wheels.app.core.analytics.data.repository.CachedUserDestinationInsightsRepository
import com.wheels.app.core.analytics.data.repository.FirebaseRoleChangeEventRepository
import com.wheels.app.core.analytics.bq13.data.connectivity.AndroidConnectivityChecker
import com.wheels.app.core.analytics.bq13.data.repository.BQ13RepositoryImpl
import com.wheels.app.core.analytics.bq13.domain.connectivity.ConnectivityChecker
import com.wheels.app.core.analytics.bq13.domain.repository.BQ13Repository
import com.wheels.app.core.analytics.bqt3.data.connectivity.BQT3ConnectivityChecker
import com.wheels.app.core.analytics.bqt3.data.repository.BQT3RepositoryImpl
import com.wheels.app.core.analytics.bqt3.domain.repository.BQT3Repository
import com.wheels.app.core.analytics.domain.repository.UserDestinationInsightsRepository
import com.wheels.app.core.analytics.domain.repository.RoleChangeEventRepository
import com.wheels.app.core.location.data.provider.FusedCurrentLocationProvider
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.network.AndroidNetworkMonitor
import com.wheels.app.core.network.NetworkMonitor
import com.wheels.app.core.session.data.repository.FirebaseUserSessionMetadataRepository
import com.wheels.app.core.session.domain.repository.UserSessionMetadataRepository
import com.wheels.app.core.trust.data.repository.FirebaseDriverTrustRepository
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.data.repository.AuthRepositoryImpl
import com.wheels.app.features.favoriteDrivers.analytics.data.repository.FavoriteDriverAnalyticsRepositoryImpl
import com.wheels.app.features.favoriteDrivers.analytics.domain.repository.FavoriteDriverAnalyticsRepository
import com.wheels.app.features.favoriteDrivers.data.repository.FavoriteDriverRepositoryImpl
import com.wheels.app.features.favoriteDrivers.domain.repository.FavoriteDriverRepository
import com.wheels.app.features.messages.data.repository.FirestoreRideMessageRepository
import com.wheels.app.features.payments.data.repository.PaymentRepositoryImpl
import com.wheels.app.features.reviews.data.repository.FirestoreRideReviewRepository
import com.wheels.app.features.rides.data.repository.FirebaseCancellationBehaviorRepository
import com.wheels.app.features.rides.data.repository.RideRepositoryImpl
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.payments.domain.repository.PaymentRepository
import com.wheels.app.features.messages.domain.repository.MessageRepository
import com.wheels.app.features.reviews.domain.repository.RideReviewRepository
import com.wheels.app.features.rides.domain.repository.CancellationBehaviorRepository
import com.wheels.app.features.rides.domain.repository.RideRepository
import com.wheels.app.features.profile.data.repository.FirebaseUserProfileRepository
import com.wheels.app.features.profile.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserDestinationInsightsRepository(
        impl: CachedUserDestinationInsightsRepository
    ): UserDestinationInsightsRepository

    @Binds
    @Singleton
    abstract fun bindBQ13Repository(impl: BQ13RepositoryImpl): BQ13Repository

    @Binds
    @Singleton
    abstract fun bindBQ13ConnectivityChecker(impl: AndroidConnectivityChecker): ConnectivityChecker

    @Binds
    @Singleton
    abstract fun bindBQT3Repository(impl: BQT3RepositoryImpl): BQT3Repository

    @Binds
    @Singleton
    abstract fun bindBQT3ConnectivityChecker(
        impl: BQT3ConnectivityChecker
    ): com.wheels.app.core.analytics.bqt3.domain.connectivity.ConnectivityChecker

    @Binds
    @Singleton
    abstract fun bindRoleChangeEventRepository(
        impl: FirebaseRoleChangeEventRepository
    ): RoleChangeEventRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: FirebaseUserProfileRepository
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindRideRepository(impl: RideRepositoryImpl): RideRepository

    @Binds
    @Singleton
    abstract fun bindCancellationBehaviorRepository(
        impl: FirebaseCancellationBehaviorRepository
    ): CancellationBehaviorRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindRideReviewRepository(
        impl: FirestoreRideReviewRepository
    ): RideReviewRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteDriverRepository(
        impl: FavoriteDriverRepositoryImpl
    ): FavoriteDriverRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteDriverAnalyticsRepository(
        impl: FavoriteDriverAnalyticsRepositoryImpl
    ): FavoriteDriverAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindDriverTrustRepository(impl: FirebaseDriverTrustRepository): DriverTrustRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: FirestoreRideMessageRepository
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindCurrentLocationProvider(impl: FusedCurrentLocationProvider): CurrentLocationProvider

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindUserSessionMetadataRepository(
        impl: FirebaseUserSessionMetadataRepository
    ): UserSessionMetadataRepository
}
