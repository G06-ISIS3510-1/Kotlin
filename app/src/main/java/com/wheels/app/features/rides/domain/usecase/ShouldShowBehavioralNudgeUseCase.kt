package com.wheels.app.features.rides.domain.usecase

import com.wheels.app.features.rides.domain.model.BehavioralNudge
import com.wheels.app.features.rides.domain.model.CancellationBehaviorMetrics
import javax.inject.Inject

class ShouldShowBehavioralNudgeUseCase @Inject constructor() {

    operator fun invoke(metrics: CancellationBehaviorMetrics?): BehavioralNudge? {
        if (metrics == null) return null

        val shouldShow = shouldShowBehavioralNudge(
            averageHoursBeforeCancellation = metrics.averageHoursBeforeCancellation,
            cancellationCount = metrics.cancellationCount
        )

        if (!shouldShow) return null

        return BehavioralNudge(
            title = "Plan changes early when possible",
            message = "You have previously tended to cancel rides within a short time before departure. If your plans are not fully confirmed, please keep in mind that early cancellations help other students reorganize in time."
        )
    }

    fun shouldShowBehavioralNudge(
        averageHoursBeforeCancellation: Double,
        cancellationCount: Int
    ): Boolean {
        return averageHoursBeforeCancellation in 0.0..24.0 && cancellationCount >= 2
    }
}
