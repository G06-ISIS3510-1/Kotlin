package com.wheels.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wheels.app.core.analytics.bqt3.data.local.BQT3UsageDao
import com.wheels.app.core.analytics.bqt3.data.local.BQT3UsageEventEntity
import com.wheels.app.core.analytics.bqt3.data.local.BQT3WeeklyUsageEntity
import com.wheels.app.core.analytics.data.local.UserDestinationInsightsDao
import com.wheels.app.core.analytics.data.local.UserDestinationInsightsEntity
import com.wheels.app.features.favoriteDrivers.data.local.FavoriteDriverDao
import com.wheels.app.features.favoriteDrivers.data.local.FavoriteDriverEntity
import com.wheels.app.features.rides.data.local.NearRidesCacheEntity
import com.wheels.app.features.rides.data.local.CreateRideDraftEntity
import com.wheels.app.features.rides.data.local.PendingRideActionEntity
import com.wheels.app.features.rides.data.local.PendingRidePublishEntity
import com.wheels.app.features.rides.data.local.RideOfflineDao

@Database(
    entities = [
        BQT3UsageEventEntity::class,
        BQT3WeeklyUsageEntity::class,
        NearRidesCacheEntity::class,
        CreateRideDraftEntity::class,
        PendingRideActionEntity::class,
        PendingRidePublishEntity::class,
        UserDestinationInsightsEntity::class,
        FavoriteDriverEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class WheelsDatabase : RoomDatabase() {
    abstract fun bqT3UsageDao(): BQT3UsageDao
    abstract fun rideOfflineDao(): RideOfflineDao
    abstract fun userDestinationInsightsDao(): UserDestinationInsightsDao
    abstract fun favoriteDriverDao(): FavoriteDriverDao
}
