package com.wheels.app.features.profile.domain.repository

import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
}
