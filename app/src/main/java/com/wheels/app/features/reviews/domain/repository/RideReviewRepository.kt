package com.wheels.app.features.reviews.domain.repository

import com.wheels.app.features.reviews.domain.model.DriverReviewSummary
import com.wheels.app.features.reviews.domain.model.RideReview
import com.wheels.app.features.reviews.domain.model.SubmitRideReviewRequest
import kotlinx.coroutines.flow.Flow

interface RideReviewRepository {
    fun observeDriverReviews(driverId: String): Flow<List<RideReview>>
    fun observeDriverReviewSummaries(): Flow<Map<String, DriverReviewSummary>>
    suspend fun submitReview(request: SubmitRideReviewRequest): RideReview
}
