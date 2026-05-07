package com.wheels.app.features.reviews.domain.usecase

import com.wheels.app.features.reviews.domain.model.ReviewDraft
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewSubmission
import com.wheels.app.features.reviews.domain.model.TripContext
import com.wheels.app.features.reviews.domain.repository.ReviewComposerRepository
import kotlinx.coroutines.flow.Flow

class ObserveAvailableTripsUseCase(
    private val repository: ReviewComposerRepository
) {
    operator fun invoke(): Flow<List<TripContext>> = repository.observeTrips()
}

class ObserveCurrentDraftUseCase(
    private val repository: ReviewComposerRepository
) {
    operator fun invoke(): Flow<ReviewDraft?> = repository.observeCurrentDraft()
}

class ObserveReviewHistoryUseCase(
    private val repository: ReviewComposerRepository
) {
    operator fun invoke(): Flow<List<ReviewSubmission>> = repository.observeSubmissions()
}

class ObserveReviewInsightsUseCase(
    private val repository: ReviewComposerRepository
) {
    operator fun invoke(): Flow<List<ReviewInsight>> = repository.observeInsights()
}

class SetActiveTripUseCase(
    private val repository: ReviewComposerRepository
) {
    suspend operator fun invoke(tripId: String) {
        repository.setActiveTrip(tripId)
    }
}

class SaveReviewDraftUseCase(
    private val repository: ReviewComposerRepository
) {
    suspend operator fun invoke(draft: ReviewDraft) {
        repository.saveDraft(draft)
    }
}

class SubmitReviewDraftUseCase(
    private val repository: ReviewComposerRepository
) {
    suspend operator fun invoke(): ReviewSubmission? = repository.submitDraft()
}

class DiscardReviewDraftUseCase(
    private val repository: ReviewComposerRepository
) {
    suspend operator fun invoke(tripId: String) {
        repository.discardDraft(tripId)
    }
}

class SeedReviewsFromLastTripUseCase(
    private val repository: ReviewComposerRepository
) {
    suspend operator fun invoke() {
        repository.seedFromLastTrip()
    }
}

