package com.wheels.app.data.remote.mapper

import com.wheels.app.data.remote.dto.RideDto
import com.wheels.app.domain.model.Ride
import java.time.Instant

fun RideDto.toDomain(): Ride = Ride(
    id = id,
    driverId = driverId,
    origin = origin,
    destination = destination,
    departureTime = Instant.parse(departureTimeIso),
    availableSeats = availableSeats,
    pricePerSeat = pricePerSeat
)
