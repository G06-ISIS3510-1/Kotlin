package com.wheels.app.features.auth.data.repository

import com.wheels.app.core.common.Resource
import com.wheels.app.features.auth.data.remote.mapper.toDto
import com.wheels.app.features.auth.domain.model.CreateAccountRequest
import com.wheels.app.features.auth.domain.model.ForgotPasswordRequest
import com.wheels.app.features.auth.domain.model.SignInRequest
import com.wheels.app.features.profile.domain.model.User
import com.wheels.app.features.auth.domain.repository.AuthRepository
import com.wheels.app.features.profile.data.remote.dto.UserDto
import com.wheels.app.features.profile.data.remote.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    override fun getCurrentUser(): Flow<User?> = flowOf(
        User(
            id = "u_001",
            fullName = "Estudiante Uniandes",
            email = "estudiante@uniandes.edu.co",
            universityId = "2026XXXX",
            rating = 4.8,
            ridesCompleted = 24,
            isDriver = true
        )
    )

    override suspend fun createAccount(request: CreateAccountRequest): Resource<User> {
        val requestDto = request.toDto()
        if (!requestDto.email.contains("@") || !requestDto.email.contains(".")) {
            return Resource.Error("Invalid university email.")
        }
        if (requestDto.password.length < 8) {
            return Resource.Error("Password must contain at least 8 characters.")
        }
        if (requestDto.phone.isBlank()) {
            return Resource.Error("Enter your phone number.")
        }

        val createdUser = UserDto(
            id = "u_new",
            fullName = requestDto.fullName,
            email = requestDto.email,
            universityId = "2026NEW",
            rating = 5.0,
            ridesCompleted = 0,
            isDriver = requestDto.isDriver
        )
        return Resource.Success(createdUser.toDomain())
    }

    override suspend fun signIn(request: SignInRequest): Resource<User> {
        val requestDto = request.toDto()
        if (!requestDto.email.contains("@") || !requestDto.email.contains(".")) {
            return Resource.Error("Enter a valid university email.")
        }
        if (requestDto.password.length < 8) {
            return Resource.Error("Enter a valid password.")
        }

        val signedInUser = UserDto(
            id = "u_001",
            fullName = "Estudiante Uniandes",
            email = requestDto.email,
            universityId = "2026XXXX",
            rating = 4.8,
            ridesCompleted = 24,
            isDriver = false
        )
        return Resource.Success(signedInUser.toDomain())
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Resource<Unit> {
        val requestDto = request.toDto()
        if (!requestDto.email.contains("@") || !requestDto.email.contains(".")) {
            return Resource.Error("Enter a valid university email.")
        }
        return Resource.Success(Unit)
    }
}
