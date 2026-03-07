package com.wheels.app.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val universityId: String,
    val rating: Double,
    val ridesCompleted: Int,
    val isDriver: Boolean
)
