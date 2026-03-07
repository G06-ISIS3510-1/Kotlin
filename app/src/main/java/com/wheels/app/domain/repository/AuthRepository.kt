package com.wheels.app.domain.repository

import com.wheels.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
}
