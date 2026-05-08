package com.wheels.app.features.rides.domain.model

data class CreateRideDraft(
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
    val description: String
) {
    val isEmpty: Boolean
        get() = origin.isBlank() &&
            destination.isBlank() &&
            date.isBlank() &&
            time.isBlank() &&
            pricePerSeat.isBlank() &&
            carModel.isBlank() &&
            licensePlate.isBlank() &&
            description.isBlank() &&
            !usedCurrentLocationOrigin &&
            !usedCurrentLocationDestination &&
            totalSeats == 3
}
