package com.wheels.app.features.rides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.rides.domain.model.CreateRideDraft
import com.wheels.app.features.rides.domain.model.CreateRideDraftSummary

@Entity(tableName = "create_ride_drafts")
data class CreateRideDraftEntity(
    @PrimaryKey val draftId: String,
    val driverId: String,
    val origin: String,
    val destination: String,
    val usedCurrentLocationOrigin: Boolean,
    val usedCurrentLocationDestination: Boolean,
    val date: String,
    val time: String,
    val totalSeats: Int,
    val pricePerSeat: String,
    val carModel: String,
    val licensePlate: String,
    val description: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun CreateRideDraftEntity.toDomain(): CreateRideDraft {
    return CreateRideDraft(
        draftId = draftId,
        driverId = driverId,
        origin = origin,
        destination = destination,
        usedCurrentLocationOrigin = usedCurrentLocationOrigin,
        usedCurrentLocationDestination = usedCurrentLocationDestination,
        date = date,
        time = time,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        description = description,
        createdAtMillis = createdAtMillis
    )
}

fun CreateRideDraftEntity.toSummary(): CreateRideDraftSummary {
    return CreateRideDraftSummary(
        draftId = draftId,
        driverId = driverId,
        origin = origin,
        destination = destination,
        usedCurrentLocationOrigin = usedCurrentLocationOrigin,
        usedCurrentLocationDestination = usedCurrentLocationDestination,
        date = date,
        time = time,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        description = description,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

fun CreateRideDraft.toEntity(nowMillis: Long = System.currentTimeMillis()): CreateRideDraftEntity {
    return CreateRideDraftEntity(
        draftId = draftId,
        driverId = driverId,
        origin = origin,
        destination = destination,
        usedCurrentLocationOrigin = usedCurrentLocationOrigin,
        usedCurrentLocationDestination = usedCurrentLocationDestination,
        date = date,
        time = time,
        totalSeats = totalSeats,
        pricePerSeat = pricePerSeat,
        carModel = carModel,
        licensePlate = licensePlate,
        description = description,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = nowMillis
    )
}
