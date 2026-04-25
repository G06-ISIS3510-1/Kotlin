package com.wheels.app.features.reviews.data.repository

import com.wheels.app.features.reviews.domain.model.ReviewAspectDefinition
import com.wheels.app.features.reviews.domain.model.ReviewAudience
import com.wheels.app.features.reviews.domain.model.ReviewComposerCopy
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewStatus
import com.wheels.app.features.reviews.domain.model.ReviewSubmission
import com.wheels.app.features.reviews.domain.model.ReviewDraft
import com.wheels.app.features.reviews.domain.model.ReviewTagDefinition
import com.wheels.app.features.reviews.domain.model.TripContext
import com.wheels.app.features.reviews.domain.model.buildBlankDraft
import com.wheels.app.features.reviews.domain.model.buildReviewChecklist
import com.wheels.app.features.reviews.domain.model.buildSubmissionHeadline
import com.wheels.app.features.reviews.domain.model.buildSubmittingDraft
import com.wheels.app.features.reviews.domain.model.reviewCompletionMessage
import com.wheels.app.features.reviews.domain.repository.ReviewComposerRepository
import com.wheels.app.features.reviews.presentation.mock.ReviewComposerPreviewData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class InMemoryReviewComposerRepository : ReviewComposerRepository {

    private val trips = MutableStateFlow(ReviewComposerPreviewData.seedTrips)
    private val currentDraft = MutableStateFlow<ReviewDraft?>(buildBlankDraft(ReviewComposerPreviewData.seedTrips.first().id))
    private val submissions = MutableStateFlow(ReviewComposerPreviewData.seedSubmissions)
    private val insights = MutableStateFlow(buildInsights(submissions.value))

    override fun observeTrips(): Flow<List<TripContext>> = trips.asStateFlow()

    override fun observeCurrentDraft(): Flow<ReviewDraft?> = currentDraft.asStateFlow()

    override fun observeSubmissions(): Flow<List<ReviewSubmission>> = submissions.asStateFlow()

    override fun observeInsights(): Flow<List<ReviewInsight>> = insights.asStateFlow()

    override suspend fun setActiveTrip(tripId: String) {
        val trip = trips.value.firstOrNull { it.id == tripId } ?: return
        currentDraft.value = currentDraft.value?.takeIf { it.tripId == tripId } ?: buildBlankDraft(trip.id)
    }

    override suspend fun saveDraft(draft: ReviewDraft) {
        currentDraft.value = draft.copy(
            updatedAtLabel = "Just now",
            status = if (draft.canSubmit) ReviewStatus.READY_TO_SUBMIT else ReviewStatus.DRAFT
        )
    }

    override suspend fun submitDraft(): ReviewSubmission? {
        val draft = currentDraft.value ?: return null
        val trip = trips.value.firstOrNull { it.id == draft.tripId } ?: return null
        if (!draft.canSubmit) return null

        val submission = ReviewSubmission(
            id = UUID.randomUUID().toString(),
            tripId = draft.tripId,
            tripRouteLabel = trip.routeName,
            driverName = trip.driverName,
            driverInitials = trip.driverInitials,
            rating = draft.rating,
            comment = draft.comment.trim(),
            audience = draft.audience,
            visibility = draft.visibility,
            selectedAspectIds = draft.selectedAspectIds.toList(),
            selectedTagIds = draft.selectedTagIds.toList(),
            isAnonymous = draft.isAnonymous,
            submittedAtLabel = "Just now",
            helpfulVotes = 0,
            replyCount = 0,
            status = ReviewStatus.SUBMITTED
        )

        submissions.update { listOf(submission) + it }
        insights.value = buildInsights(submissions.value)
        currentDraft.value = buildBlankDraft(trip.id)
        return submission
    }

    override suspend fun discardDraft(tripId: String) {
        val trip = trips.value.firstOrNull { it.id == tripId } ?: return
        currentDraft.value = buildBlankDraft(trip.id)
    }

    override suspend fun seedFromLastTrip() {
        val trip = trips.value.firstOrNull() ?: return
        currentDraft.value = currentDraft.value?.takeIf { it.tripId == trip.id } ?: buildBlankDraft(trip.id)
    }

    private fun buildInsights(submissions: List<ReviewSubmission>): List<ReviewInsight> {
        if (submissions.isEmpty()) {
            return listOf(
                ReviewInsight(
                    title = "No reviews yet",
                    value = "0",
                    subtitle = "Use the form to publish the first trip review.",
                    trendLabel = "Starting point",
                    positiveTrend = true
                ),
                ReviewInsight(
                    title = "Average rating",
                    value = "--",
                    subtitle = "The local store will calculate this after submissions.",
                    trendLabel = "Waiting for data",
                    positiveTrend = true
                )
            )
        }

        val average = submissions.map { it.rating }.average()
        val fiveStarCount = submissions.count { it.rating == 5 }
        val publicCount = submissions.count { it.visibility == com.wheels.app.features.reviews.domain.model.ReviewVisibility.PUBLIC }
        val anonymousCount = submissions.count { it.isAnonymous }
        val helpfulVotes = submissions.sumOf { it.helpfulVotes }
        val replyCount = submissions.sumOf { it.replyCount }
        val positiveRatio = submissions.count { it.rating >= 4 }.toDouble() / submissions.size.toDouble()

        return listOf(
            ReviewInsight(
                title = "Average rating",
                value = String.format("%.1f", average),
                subtitle = "Across ${submissions.size} submitted reviews",
                trendLabel = if (positiveRatio >= 0.75) "Strong streak" else "Mixed feedback",
                positiveTrend = positiveRatio >= 0.75
            ),
            ReviewInsight(
                title = "Five star reviews",
                value = fiveStarCount.toString(),
                subtitle = "Ratings that can be reused as trust signals",
                trendLabel = if (fiveStarCount >= submissions.size / 2) "Excellent" else "Growing",
                positiveTrend = fiveStarCount >= submissions.size / 2
            ),
            ReviewInsight(
                title = "Public reviews",
                value = publicCount.toString(),
                subtitle = "Visible in the passenger and driver histories",
                trendLabel = if (publicCount >= 3) "Healthy mix" else "Low visibility",
                positiveTrend = true
            ),
            ReviewInsight(
                title = "Anonymous reviews",
                value = anonymousCount.toString(),
                subtitle = "Feedback sent without attaching a public identity",
                trendLabel = if (anonymousCount == 0) "No anonymous posts" else "Privacy aware",
                positiveTrend = true
            ),
            ReviewInsight(
                title = "Community reactions",
                value = helpfulVotes.toString(),
                subtitle = "Helpful votes and replies in the review history",
                trendLabel = if (replyCount > 0) "$replyCount replies" else "No replies yet",
                positiveTrend = helpfulVotes >= 10
            )
        )
    }
}
