package com.wheels.app.features.rides.presentation.viewmodel

val mockRideRequestData = mapOf(
    "1" to RideRequestUiModel(
        id = "1",
        origin = "Campus Uniandes - Main Gate",
        destination = "Centro Comercial Andino",
        departureTime = "14:30",
        departureDate = "Today",
        estimatedDuration = "30 min",
        estimatedArrival = "15:00",
        price = 3500,
        availableSeats = 3,
        totalSeats = 4,
        distance = "4.2 km",
        route = listOf("Campus Uniandes", "Calle 85", "Av. 15", "Centro Andino"),
        amenities = listOf("AC", "Music", "Phone charger"),
        cancellationPolicy = "Free cancellation up to 30 min before departure",
        driver = RideDriverUiModel(
            name = "Carlos Mendez",
            rating = 4.8,
            ridesCount = 124,
            reliabilityScore = 98,
            memberSince = "Jan 2024",
            carModel = "Toyota Corolla 2020",
            carColor = "Silver",
            licensePlate = "ABC-123",
            punctualityRate = 96,
            responseTime = "2 min"
        )
    ),
    "2" to RideRequestUiModel(
        id = "2",
        origin = "Campus Uniandes - Main Gate",
        destination = "Usaquen",
        departureTime = "15:00",
        departureDate = "Today",
        estimatedDuration = "35 min",
        estimatedArrival = "15:35",
        price = 4000,
        availableSeats = 2,
        totalSeats = 3,
        distance = "5.8 km",
        route = listOf("Campus Uniandes", "Av. 7", "Calle 116", "Usaquen"),
        amenities = listOf("AC", "Wifi"),
        cancellationPolicy = "Free cancellation up to 30 min before departure",
        driver = RideDriverUiModel(
            name = "Maria Sanchez",
            rating = 4.9,
            ridesCount = 89,
            reliabilityScore = 99,
            memberSince = "Mar 2024",
            carModel = "Mazda 3 2021",
            carColor = "Red",
            licensePlate = "XYZ-456",
            punctualityRate = 98,
            responseTime = "1 min"
        )
    ),
    "3" to RideRequestUiModel(
        id = "3",
        origin = "Campus Uniandes - Main Gate",
        destination = "Suba",
        departureTime = "15:30",
        departureDate = "Today",
        estimatedDuration = "45 min",
        estimatedArrival = "16:15",
        price = 5000,
        availableSeats = 1,
        totalSeats = 4,
        distance = "8.5 km",
        route = listOf("Campus Uniandes", "Av. 68", "Calle 145", "Suba Centro"),
        amenities = listOf("Music"),
        cancellationPolicy = "Free cancellation up to 30 min before departure",
        driver = RideDriverUiModel(
            name = "Juan Pablo",
            rating = 4.7,
            ridesCount = 67,
            reliabilityScore = 94,
            memberSince = "May 2024",
            carModel = "Chevrolet Spark 2019",
            carColor = "White",
            licensePlate = "DEF-789",
            punctualityRate = 92,
            responseTime = "3 min"
        )
    )
)
