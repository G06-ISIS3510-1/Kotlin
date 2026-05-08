package com.wheels.app.features.profile.domain.repository

import com.wheels.app.features.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    // Exposes the current profile as a reactive stream for offline-first UI updates.
    fun observeCurrentUserProfile(): Flow<User?>

    // Forces a remote refresh and writes the result back into local cache.
    suspend fun refreshCurrentUserProfile()

    // Clears the cached profile when the user signs out.
    suspend fun clearCachedProfile()
}
