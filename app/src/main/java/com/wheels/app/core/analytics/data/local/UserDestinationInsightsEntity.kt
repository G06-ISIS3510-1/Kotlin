package com.wheels.app.core.analytics.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.core.analytics.domain.model.DestinationInsight
import com.wheels.app.core.analytics.domain.model.UserDestinationInsights

@Entity(tableName = "user_destination_insights")
data class UserDestinationInsightsEntity(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "topDestinationsJson")
    val topDestinationsJson: String,

    @ColumnInfo(name = "totalBookingsTracked")
    val totalBookingsTracked: Int,

    @ColumnInfo(name = "lastUpdatedMillis")
    val lastUpdatedMillis: Long
)

fun UserDestinationInsightsEntity.toDomain(): UserDestinationInsights {
    val destinations = try {
        topDestinationsJson.split(";")
            .mapNotNull { part ->
                val pieces = part.split("|")
                if (pieces.size >= 3) {
                    DestinationInsight(
                        destinationName = pieces[0],
                        bookingCount = pieces[1].toIntOrNull() ?: 0,
                        rank = pieces[2].toIntOrNull() ?: 0
                    )
                } else null
            }
    } catch (_: Throwable) {
        emptyList()
    }

    return UserDestinationInsights(
        userId = userId,
        topDestinations = destinations,
        totalBookingsTracked = totalBookingsTracked,
        lastUpdatedMillis = lastUpdatedMillis
    )
}

fun UserDestinationInsights.toEntity(): UserDestinationInsightsEntity {
    val json = topDestinations.joinToString(separator = ";") { d -> "${d.destinationName}|${d.bookingCount}|${d.rank}" }
    return UserDestinationInsightsEntity(
        userId = userId,
        topDestinationsJson = json,
        totalBookingsTracked = totalBookingsTracked,
        lastUpdatedMillis = lastUpdatedMillis ?: System.currentTimeMillis()
    )
}
