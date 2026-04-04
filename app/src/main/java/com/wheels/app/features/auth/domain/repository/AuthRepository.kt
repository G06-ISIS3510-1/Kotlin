package com.wheels.app.features.auth.domain.repository

import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.domain.model.AuthUser
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthSession(): Flow<AuthUser?>

    fun getCurrentUser(): Flow<User?>
    fun getLoginHistory(): List<Long>

    suspend fun restoreSession(): AuthUser?

    suspend fun createAccount(request: CreateAccountRequest): Resource<AuthUser>

    suspend fun signIn(request: SignInRequest): Resource<AuthUser>

    suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<Unit>

    suspend fun signOut()
}
