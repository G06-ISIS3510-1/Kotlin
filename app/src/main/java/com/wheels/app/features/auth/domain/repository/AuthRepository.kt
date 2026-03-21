package com.wheels.app.features.auth.domain.repository

import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    fun getLoginHistory(): List<Long>

    suspend fun createAccount(request: CreateAccountRequest): Resource<User>

    suspend fun signIn(request: SignInRequest): Resource<User>

    suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<Unit>
}
