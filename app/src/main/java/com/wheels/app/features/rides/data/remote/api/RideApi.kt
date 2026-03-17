package com.wheels.app.features.rides.data.remote.api

import com.wheels.app.features.rides.data.remote.dto.RideDto
import retrofit2.http.GET

interface RideApi {
    @GET("rides/available")
    suspend fun getAvailableRides(): List<RideDto>
}
