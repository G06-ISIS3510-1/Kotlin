package com.wheels.app.data.remote.api

import com.wheels.app.data.remote.dto.UserDto
import retrofit2.http.GET

interface AuthApi {
    @GET("auth/me")
    suspend fun getCurrentUser(): UserDto
}
