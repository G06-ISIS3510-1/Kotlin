package com.wheels.app.core.session.domain.repository

interface UserSessionMetadataRepository {
    suspend fun syncCurrentSessionMetadata()
    suspend fun updateCurrentUserFcmToken(token: String)
}
