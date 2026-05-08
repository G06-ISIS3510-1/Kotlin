package com.wheels.app.features.profile.domain.repository

import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeCurrentUserProfile(): Flow<User?>

    suspend fun refreshCurrentUserProfile()

    suspend fun clearCachedProfile()
}
