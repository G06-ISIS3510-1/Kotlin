package com.wheels.app.features.rides.data.remote.api

import com.wheels.app.features.rides.data.remote.dto.RideDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RideApi {
    @GET("rides/available")
    suspend fun getAvailableRides(): List<RideDto>

    @GET("rides/near")
    suspend fun getNearRides(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radiusMeters") radiusMeters: Int,
        @Query("destination") destinationQuery: String? = null
    ): List<RideDto>
}
