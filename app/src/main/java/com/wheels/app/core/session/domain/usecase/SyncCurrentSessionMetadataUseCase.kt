package com.wheels.app.core.session.domain.usecase

import com.wheels.app.core.session.domain.repository.UserSessionMetadataRepository
import javax.inject.Inject

class SyncCurrentSessionMetadataUseCase @Inject constructor(
    private val repository: UserSessionMetadataRepository
) {
    suspend operator fun invoke() {
        repository.syncCurrentSessionMetadata()
    }
}
