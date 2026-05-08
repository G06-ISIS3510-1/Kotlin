package com.wheels.app.features.reviews.di

import com.wheels.app.features.reviews.data.local.DeviceMemoryReviewsCacheSizer
import com.wheels.app.features.reviews.data.local.ReviewsCacheSizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewsCacheModule {

    @Binds
    @Singleton
    abstract fun bindReviewsCacheSizer(
        impl: DeviceMemoryReviewsCacheSizer
    ): ReviewsCacheSizer
}
