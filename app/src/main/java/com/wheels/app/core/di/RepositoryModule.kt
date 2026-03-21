package com.wheels.app.core.di

import com.wheels.app.core.location.data.provider.FusedCurrentLocationProvider
import com.wheels.app.core.location.domain.provider.CurrentLocationProvider
import com.wheels.app.core.trust.data.repository.FirebaseDriverTrustRepository
import com.wheels.app.core.trust.domain.repository.DriverTrustRepository
import com.wheels.app.features.auth.data.repository.AuthRepositoryImpl
import com.wheels.app.features.payments.data.repository.PaymentRepositoryImpl
import com.wheels.app.features.rides.data.repository.RideRepositoryImpl
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.payments.domain.repository.PaymentRepository
import com.wheels.app.features.rides.domain.repository.RideRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRideRepository(impl: RideRepositoryImpl): RideRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindDriverTrustRepository(impl: FirebaseDriverTrustRepository): DriverTrustRepository

    @Binds
    @Singleton
    abstract fun bindCurrentLocationProvider(impl: FusedCurrentLocationProvider): CurrentLocationProvider
}
