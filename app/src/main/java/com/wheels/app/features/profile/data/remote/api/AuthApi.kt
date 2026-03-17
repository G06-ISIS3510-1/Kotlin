package com.wheels.app.features.profile.data.remote.api

import com.wheels.app.features.profile.data.remote.dto.UserDto
import retrofit2.http.GET

interface AuthApi {
    @GET("auth/me")
    suspend fun getCurrentUser(): UserDto
}
