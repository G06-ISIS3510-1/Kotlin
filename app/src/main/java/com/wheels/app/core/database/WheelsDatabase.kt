package com.wheels.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wheels.app.core.analytics.bqt3.data.local.BQT3UsageDao
import com.wheels.app.core.analytics.bqt3.data.local.BQT3UsageEventEntity
import com.wheels.app.core.analytics.bqt3.data.local.BQT3WeeklyUsageEntity

@Database(
    entities = [
        BQT3UsageEventEntity::class,
        BQT3WeeklyUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WheelsDatabase : RoomDatabase() {
    abstract fun bqT3UsageDao(): BQT3UsageDao
}
