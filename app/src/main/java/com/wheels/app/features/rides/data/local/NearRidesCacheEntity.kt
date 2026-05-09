package com.wheels.app.features.rides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheels.app.features.rides.domain.model.Coordinates
import com.wheels.app.features.rides.domain.model.Ride
import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "near_rides_cache")
data class NearRidesCacheEntity(
    @PrimaryKey val cacheKey: String,
    val ridesJson: String,
    val cachedAtMillis: Long
)

fun NearRidesCacheKey.toStorageKey(): String {
    return "$latBucket|$lngBucket|$radiusMeters|$destinationQuery"
}

fun CachedNearRides.toEntity(cacheKey: NearRidesCacheKey): NearRidesCacheEntity {
    return NearRidesCacheEntity(
        cacheKey = cacheKey.toStorageKey(),
        ridesJson = rides.toJsonArray().toString(),
        cachedAtMillis = cachedAtMillis
    )
}

fun NearRidesCacheEntity.toCachedNearRides(): CachedNearRides {
    return CachedNearRides(
        rides = ridesJson.toRideList(),
        cachedAtMillis = cachedAtMillis
    )
}

private fun List<Ride>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        forEach { ride ->
            put(ride.toJson())
        }
    }
}

private fun String.toRideList(): List<Ride> {
    if (isBlank()) return emptyList()

    val jsonArray = JSONArray(this)
    return buildList(jsonArray.length()) {
        for (index in 0 until jsonArray.length()) {
            add(jsonArray.getJSONObject(index).toRide())
        }
    }
}

private fun Ride.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("driverId", driverId)
        put("driverName", driverName)
        put("driverEmail", driverEmail)
        put("driverRating", driverRating)
        put("reviewCount", reviewCount)
        put("reliabilityScore", reliabilityScore)
        put("status", status)
        put("origin", origin)
        put("originCoordinates", originCoordinates?.toJson() ?: JSONObject.NULL)
        put("destination", destination)
        put("destinationCoordinates", destinationCoordinates?.toJson() ?: JSONObject.NULL)
        put("destinationArea", destinationArea)
        put("departureTimeMillis", departureTime.toEpochMilli())
        put("estimatedDurationMinutes", estimatedDurationMinutes)
        put("availableSeats", availableSeats)
        put("totalSeats", totalSeats)
        put("pricePerSeat", pricePerSeat)
        put("punctualityRate", punctualityRate)
        put("isHabitRide", isHabitRide)
        put("carModel", carModel)
        put("licensePlate", licensePlate)
        put("notes", notes)
        put("verifiedByUniversity", verifiedByUniversity)
        put("paymentOption", paymentOption)
        put("passengerIds", JSONArray(passengerIds))
    }
}

private fun JSONObject.toRide(): Ride {
    return Ride(
        id = getString("id"),
        driverId = getString("driverId"),
        driverName = optString("driverName", ""),
        driverEmail = optString("driverEmail", ""),
        driverRating = optDouble("driverRating", 5.0),
        reviewCount = optInt("reviewCount", 0),
        reliabilityScore = optInt("reliabilityScore", 100),
        status = optString("status", "published"),
        origin = getString("origin"),
        originCoordinates = optNullableCoordinates("originCoordinates"),
        destination = getString("destination"),
        destinationCoordinates = optNullableCoordinates("destinationCoordinates"),
        destinationArea = optString("destinationArea", ""),
        departureTime = Instant.ofEpochMilli(getLong("departureTimeMillis")),
        estimatedDurationMinutes = optInt("estimatedDurationMinutes", 30),
        availableSeats = optInt("availableSeats", 0),
        totalSeats = optInt("totalSeats", 0),
        pricePerSeat = optDouble("pricePerSeat", 0.0),
        punctualityRate = optInt("punctualityRate", 100),
        isHabitRide = optBoolean("isHabitRide", false),
        carModel = optString("carModel", ""),
        licensePlate = optString("licensePlate", ""),
        notes = optString("notes", ""),
        verifiedByUniversity = optBoolean("verifiedByUniversity", true),
        paymentOption = optString("paymentOption", "card"),
        passengerIds = optJSONArray("passengerIds")?.let { array ->
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    add(array.optString(index))
                }
            }
        } ?: emptyList()
    )
}

private fun Coordinates.toJson(): JSONObject {
    return JSONObject().apply {
        put("lat", lat)
        put("lng", lng)
    }
}

private fun JSONObject.optNullableCoordinates(name: String): Coordinates? {
    val coordinatesJson = optJSONObject(name) ?: return null
    return Coordinates(
        lat = coordinatesJson.optDouble("lat", 0.0),
        lng = coordinatesJson.optDouble("lng", 0.0)
    )
}