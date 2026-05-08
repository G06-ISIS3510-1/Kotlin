package com.wheels.app.features.rides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.rides.domain.model.CreateRideDraft

@Entity(tableName = "create_ride_drafts")
data class CreateRideDraftEntity(
    @PrimaryKey val driverId: String,
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
    val updatedAtMillis: Long
)

fun CreateRideDraftEntity.toDomain(): CreateRideDraft {
    return CreateRideDraft(
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
        description = description
    )
}

fun CreateRideDraft.toEntity(nowMillis: Long = System.currentTimeMillis()): CreateRideDraftEntity {
    return CreateRideDraftEntity(
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
        updatedAtMillis = nowMillis
    )
}
