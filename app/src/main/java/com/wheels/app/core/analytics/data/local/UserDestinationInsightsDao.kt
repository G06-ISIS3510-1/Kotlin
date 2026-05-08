package com.wheels.app.core.analytics.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDestinationInsightsDao {

    @Query("SELECT * FROM user_destination_insights WHERE userId = :userId LIMIT 1")
    suspend fun getUserInsights(userId: String): UserDestinationInsightsEntity?

    @Upsert
    suspend fun upsertInsights(entity: UserDestinationInsightsEntity)

    @Query("DELETE FROM user_destination_insights WHERE userId = :userId")
    suspend fun deleteInsights(userId: String)
}
