package com.wheels.app.features.auth.data.remote.api

import com.wheels.app.features.auth.data.remote.dto.CreateAccountRequestDto
import com.wheels.app.features.auth.data.remote.dto.ForgotPasswordRequestDto
import com.wheels.app.features.auth.data.remote.dto.SignInRequestDto
import com.wheels.app.features.profile.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @GET("auth/me")
    suspend fun getCurrentUser(): UserDto

    @POST("auth/register")
    suspend fun createAccount(@Body request: CreateAccountRequestDto): UserDto

    @POST("auth/login")
    suspend fun signIn(@Body request: SignInRequestDto): UserDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto)
}
