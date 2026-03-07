package com.wheels.app.data.remote.api

import com.wheels.app.data.remote.dto.RideDto
import retrofit2.http.GET

interface RideApi {
    @GET("rides/available")
    suspend fun getAvailableRides(): List<RideDto>
}
