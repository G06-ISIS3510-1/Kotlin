package com.wheels.app.core.di

import android.content.Context
import androidx.room.Room
import com.wheels.app.core.analytics.bqt3.data.local.BQT3UsageDao
import com.wheels.app.core.database.WheelsDatabase
import com.wheels.app.features.favoriteDrivers.data.local.FavoriteDriverDao
import com.wheels.app.features.rides.data.local.RideOfflineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWheelsDatabase(
        @ApplicationContext context: Context
    ): WheelsDatabase {
        return Room.databaseBuilder(
            context,
            WheelsDatabase::class.java,
            "wheels.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideBQT3UsageDao(database: WheelsDatabase): BQT3UsageDao {
        return database.bqT3UsageDao()
    }

    @Provides
    fun provideRideOfflineDao(database: WheelsDatabase): RideOfflineDao {
        return database.rideOfflineDao()
    }

    @Provides
    fun provideUserDestinationInsightsDao(database: WheelsDatabase): com.wheels.app.core.analytics.data.local.UserDestinationInsightsDao {
        return database.userDestinationInsightsDao()
    }

    @Provides
    fun provideFavoriteDriverDao(database: WheelsDatabase): FavoriteDriverDao {
        return database.favoriteDriverDao()
    }
}
