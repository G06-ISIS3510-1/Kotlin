package com.wheels.app.data.repository

import com.wheels.app.domain.model.User
import com.wheels.app.domain.repository.AuthRepository
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
}
