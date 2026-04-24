package com.wheels.app.core.analytics.bqt3.domain.usecase

import com.wheels.app.core.analytics.bqt3.domain.repository.BQT3Repository
import javax.inject.Inject

class SyncPendingEventsUseCase @Inject constructor(
    private val repository: BQT3Repository
) {
    suspend operator fun invoke() {
        repository.syncPendingEvents()
    }
}
