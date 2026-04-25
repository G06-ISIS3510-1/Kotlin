package com.wheels.app.core.session.domain.usecase

import com.wheels.app.core.session.domain.repository.UserSessionMetadataRepository
import javax.inject.Inject

class UpdateCurrentUserFcmTokenUseCase @Inject constructor(
    private val repository: UserSessionMetadataRepository
) {
    suspend operator fun invoke(token: String) {
        repository.updateCurrentUserFcmToken(token)
    }
}
