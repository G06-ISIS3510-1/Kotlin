package com.wheels.app.core.di

import com.wheels.app.core.behavior.data.bus.InMemoryEventBus
import com.wheels.app.core.behavior.data.observer.AnalyticsObserver
import com.wheels.app.core.behavior.data.observer.NotificationObserver
import com.wheels.app.core.behavior.data.observer.UsageTrackerObserver
import com.wheels.app.core.behavior.data.repository.FirebaseAppOpenAnalyticsRepository
import com.wheels.app.core.behavior.data.repository.FirebaseAppOpenNotificationRepository
import com.wheels.app.core.behavior.data.repository.FirebaseAppOpenTrackingRepository
import com.wheels.app.core.behavior.domain.bus.EventBus
import com.wheels.app.core.behavior.domain.observer.AppEventSubscriber
import com.wheels.app.core.behavior.domain.repository.AppOpenAnalyticsRepository
import com.wheels.app.core.behavior.domain.repository.AppOpenNotificationRepository
import com.wheels.app.core.behavior.domain.repository.AppOpenTrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BehaviorModule {

    @Binds
    @Singleton
    abstract fun bindEventBus(impl: InMemoryEventBus): EventBus

    @Binds
    @Singleton
    abstract fun bindAppOpenTrackingRepository(
        impl: FirebaseAppOpenTrackingRepository
    ): AppOpenTrackingRepository

    @Binds
    @Singleton
    abstract fun bindAppOpenAnalyticsRepository(
        impl: FirebaseAppOpenAnalyticsRepository
    ): AppOpenAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindAppOpenNotificationRepository(
        impl: FirebaseAppOpenNotificationRepository
    ): AppOpenNotificationRepository

    @Binds
    @IntoSet
    abstract fun bindUsageTrackerObserver(
        observer: UsageTrackerObserver
    ): AppEventSubscriber

    @Binds
    @IntoSet
    abstract fun bindAnalyticsObserver(
        observer: AnalyticsObserver
    ): AppEventSubscriber

    @Binds
    @IntoSet
    abstract fun bindNotificationObserver(
        observer: NotificationObserver
    ): AppEventSubscriber
}
