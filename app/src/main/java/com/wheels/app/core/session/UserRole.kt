package com.wheels.app.core.session

enum class UserRole {
    PASSENGER,
    DRIVER;

    val storageValue: String
        get() = name.lowercase()

    val displayName: String
        get() = when (this) {
            PASSENGER -> "Passenger"
            DRIVER -> "Driver"
        }

    companion object {
        fun fromStorageValue(value: String?): UserRole? {
            return entries.firstOrNull { it.storageValue == value?.lowercase() }
        }
    }
}
