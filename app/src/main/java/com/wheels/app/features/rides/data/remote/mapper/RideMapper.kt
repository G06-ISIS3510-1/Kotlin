package com.wheels.app.features.rides.data.remote.mapper

import com.wheels.app.features.rides.data.remote.dto.RideDto
import com.wheels.app.features.rides.domain.model.Ride
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
