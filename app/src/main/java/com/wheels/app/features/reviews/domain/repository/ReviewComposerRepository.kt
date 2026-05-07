package com.wheels.app.features.reviews.domain.repository

import com.wheels.app.features.reviews.domain.model.ReviewDraft
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewSubmission
import com.wheels.app.features.reviews.domain.model.TripContext
import kotlinx.coroutines.flow.Flow

interface ReviewComposerRepository {
    fun observeTrips(): Flow<List<TripContext>>
    fun observeCurrentDraft(): Flow<ReviewDraft?>
    fun observeSubmissions(): Flow<List<ReviewSubmission>>
    fun observeInsights(): Flow<List<ReviewInsight>>
    suspend fun setActiveTrip(tripId: String)
    suspend fun saveDraft(draft: ReviewDraft)
    suspend fun submitDraft(): ReviewSubmission?
    suspend fun discardDraft(tripId: String)
    suspend fun seedFromLastTrip()
}

