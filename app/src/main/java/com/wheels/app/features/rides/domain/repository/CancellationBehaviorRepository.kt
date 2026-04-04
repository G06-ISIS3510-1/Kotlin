package com.wheels.app.features.rides.domain.repository

import com.wheels.app.features.rides.domain.model.CancellationBehaviorMetrics
import kotlinx.coroutines.flow.Flow

interface CancellationBehaviorRepository {
    fun observeCancellationBehaviorMetrics(userId: String): Flow<CancellationBehaviorMetrics?>
}
