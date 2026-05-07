package com.wheels.app.features.reviews.domain.model

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReviewAudience(val label: String) {
    DRIVER("Driver"),
    PASSENGER("Passenger"),
    BOTH("Both")
}

enum class ReviewVisibility(val label: String) {
    PUBLIC("Public"),
    TRIP_ONLY("Trip only"),
    PRIVATE("Private")
}

enum class ReviewComposerTab(val label: String) {
    WRITE("Write review"),
    HISTORY("History"),
    INSIGHTS("Insights")
}

enum class ReviewWritingTemplate(val label: String) {
    PRAISE("Praise"),
    BALANCED("Balanced"),
    CONSTRUCTIVE("Constructive"),
    ISSUE_REPORT("Issue report"),
    SHORT_NOTE("Short note")
}

enum class ReviewHistoryFilter(val label: String) {
    ALL("All"),
    FIVE_STAR("5 stars"),
    PUBLIC("Public"),
    ANONYMOUS("Anonymous"),
    NEEDS_ATTENTION("Needs attention")
}

data class ReviewAspectDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val highlightColorHex: Long,
    val positiveHint: String,
    val negativeHint: String,
    val weight: Int = 1
)

data class ReviewTagDefinition(
    val id: String,
    val label: String,
    val helperText: String,
    val iconKey: String,
    val recommendedForStars: IntRange
)

data class TripContext(
    val id: String,
    val routeName: String,
    val origin: String,
    val destination: String,
    val dateLabel: String,
    val timeLabel: String,
    val seats: Int,
    val pricePerSeat: Int,
    val driverName: String,
    val driverInitials: String,
    val driverRating: Double,
    val vehicleLabel: String,
    val verifiedDriver: Boolean,
    val completedMinutesAgo: Int,
    val note: String = "",
    val badgeLabel: String = "Completed"
) {
    val totalFareLabel: String
        get() = "$${pricePerSeat * seats}"

    val timeSinceCompletionLabel: String
        get() = when {
            completedMinutesAgo < 60 -> "$completedMinutesAgo min ago"
            completedMinutesAgo < 1_440 -> "${completedMinutesAgo / 60} h ago"
            else -> "${completedMinutesAgo / 1_440} d ago"
        }
}

data class ReviewDraft(
    val tripId: String = "",
    val rating: Int = 0,
    val audience: ReviewAudience = ReviewAudience.DRIVER,
    val visibility: ReviewVisibility = ReviewVisibility.PUBLIC,
    val selectedAspectIds: Set<String> = emptySet(),
    val selectedTagIds: Set<String> = emptySet(),
    val comment: String = "",
    val privateNote: String = "",
    val isAnonymous: Boolean = false,
    val followUpRequested: Boolean = false,
    val createdAtLabel: String = "Now",
    val updatedAtLabel: String = "Now",
    val status: ReviewStatus = ReviewStatus.DRAFT
) {
    val commentLength: Int
        get() = comment.trim().length

    val privateNoteLength: Int
        get() = privateNote.trim().length

    val hasRating: Boolean
        get() = rating in 1..5

    val hasComment: Boolean
        get() = commentLength >= 20

    val canSubmit: Boolean
        get() = hasRating && hasComment
}

enum class ReviewStatus(val label: String) {
    DRAFT("Draft"),
    READY_TO_SUBMIT("Ready"),
    SUBMITTED("Submitted"),
    ARCHIVED("Archived")
}

data class ReviewSubmission(
    val id: String,
    val tripId: String,
    val tripRouteLabel: String,
    val driverName: String,
    val driverInitials: String,
    val rating: Int,
    val comment: String,
    val audience: ReviewAudience,
    val visibility: ReviewVisibility,
    val selectedAspectIds: List<String>,
    val selectedTagIds: List<String>,
    val isAnonymous: Boolean,
    val submittedAtLabel: String,
    val helpfulVotes: Int,
    val replyCount: Int,
    val status: ReviewStatus = ReviewStatus.SUBMITTED
) {
    val starSummaryLabel: String
        get() = "${rating}.0"

    val commentPreview: String
        get() = if (comment.length <= 96) comment else comment.take(96).trimEnd() + "..."
}

data class ReviewInsight(
    val title: String,
    val value: String,
    val subtitle: String,
    val trendLabel: String,
    val positiveTrend: Boolean
)

data class ReviewMetric(
    val title: String,
    val value: String,
    val hint: String
)

data class ReviewToneGuide(
    val title: String,
    val body: String,
    val colorLabel: String
)

data class ReviewChecklistItem(
    val title: String,
    val body: String,
    val done: Boolean
)

data class ReviewComposerCopy(
    val title: String,
    val subtitle: String,
    val completionMessage: String,
    val draftReminder: String,
    val emptyHistoryTitle: String,
    val emptyHistorySubtitle: String
)

fun TripContext.formattedPriceRangeLabel(): String {
    val formatter = DecimalFormat("#,###")
    return "$${formatter.format(pricePerSeat)} per seat"
}

fun TripContext.fullRouteLabel(): String = "$origin to $destination"

fun ReviewDraft.withRating(value: Int): ReviewDraft = copy(
    rating = value.coerceIn(0, 5),
    status = if (value in 1..5 && hasComment) ReviewStatus.READY_TO_SUBMIT else ReviewStatus.DRAFT
)

fun ReviewDraft.withAudience(value: ReviewAudience): ReviewDraft = copy(audience = value)

fun ReviewDraft.withVisibility(value: ReviewVisibility): ReviewDraft = copy(visibility = value)

fun ReviewDraft.toggleAspect(id: String): ReviewDraft {
    val updated = selectedAspectIds.toMutableSet()
    if (!updated.add(id)) {
        updated.remove(id)
    }
    return copy(selectedAspectIds = updated)
}

fun ReviewDraft.toggleTag(id: String): ReviewDraft {
    val updated = selectedTagIds.toMutableSet()
    if (!updated.add(id)) {
        updated.remove(id)
    }
    return copy(selectedTagIds = updated)
}

fun ReviewDraft.withComment(value: String): ReviewDraft = copy(
    comment = value,
    status = if (rating in 1..5 && value.trim().length >= 20) ReviewStatus.READY_TO_SUBMIT else ReviewStatus.DRAFT
)

fun ReviewDraft.withPrivateNote(value: String): ReviewDraft = copy(privateNote = value)

fun ReviewDraft.withAnonymous(value: Boolean): ReviewDraft = copy(isAnonymous = value)

fun ReviewDraft.withFollowUpRequested(value: Boolean): ReviewDraft = copy(followUpRequested = value)

fun ReviewDraft.markSubmitted(): ReviewDraft = copy(status = ReviewStatus.SUBMITTED)

fun ReviewDraft.previewSummary(): String {
    val tagSummary = if (selectedTagIds.isEmpty()) "No tags yet" else "${selectedTagIds.size} tags"
    return "${rating} stars, ${commentLength} chars, $tagSummary"
}

fun buildBlankDraft(tripId: String): ReviewDraft = ReviewDraft(
    tripId = tripId,
    createdAtLabel = "Just now",
    updatedAtLabel = "Just now"
)

fun buildSubmittingDraft(draft: ReviewDraft): ReviewDraft = draft.copy(
    status = ReviewStatus.READY_TO_SUBMIT,
    updatedAtLabel = "Just now"
)

fun buildTripDateLabel(date: String, time: String): String = "$date at $time"

fun reviewCompletionMessage(rating: Int, audience: ReviewAudience): String {
    return when {
        rating >= 5 && audience == ReviewAudience.DRIVER -> "Excellent feedback. Your driver will see a strong, detailed review."
        rating >= 4 -> "Thanks. Your feedback is balanced and helpful for future trips."
        rating == 3 -> "Your review will help improve the next ride experience."
        else -> "Your comments are especially useful when something needs attention."
    }
}

fun buildReviewChecklist(draft: ReviewDraft): List<ReviewChecklistItem> {
    return listOf(
        ReviewChecklistItem(
            title = "Rate the experience",
            body = "Choose a star rating that reflects the overall trip.",
            done = draft.hasRating
        ),
        ReviewChecklistItem(
            title = "Add useful context",
            body = "A few specific details help the other person understand what worked well.",
            done = draft.hasComment
        ),
        ReviewChecklistItem(
            title = "Pick relevant tags",
            body = "Tags make the review easier to scan when the app summarizes it later.",
            done = draft.selectedTagIds.isNotEmpty()
        )
    )
}

fun buildTemplateComment(
    template: ReviewWritingTemplate,
    trip: TripContext? = null
): String {
    val route = trip?.routeName ?: "this trip"
    val driver = trip?.driverName ?: "the driver"
    return when (template) {
        ReviewWritingTemplate.PRAISE -> "I had a great experience on $route. $driver was punctual, the ride was smooth, and the whole trip felt comfortable."
        ReviewWritingTemplate.BALANCED -> "Overall, $route was a good ride. There were a couple of small details to improve, but the experience was still positive."
        ReviewWritingTemplate.CONSTRUCTIVE -> "The trip on $route was acceptable, but there were some details around communication and timing that could be improved."
        ReviewWritingTemplate.ISSUE_REPORT -> "I need to flag an issue from $route. The pickup and timing details were confusing, and I had to follow up multiple times."
        ReviewWritingTemplate.SHORT_NOTE -> "Good ride on $route. $driver kept things clear and made the trip easy."
    }
}

fun buildTemplatePrivateNote(template: ReviewWritingTemplate): String {
    return when (template) {
        ReviewWritingTemplate.PRAISE -> "Use this review to reinforce punctuality and friendly communication."
        ReviewWritingTemplate.BALANCED -> "Mention the positive parts first, then note one or two improvements."
        ReviewWritingTemplate.CONSTRUCTIVE -> "Focus on clarity and suggest what would have made the ride better."
        ReviewWritingTemplate.ISSUE_REPORT -> "Keep a record of the exact problem in case support needs to review it."
        ReviewWritingTemplate.SHORT_NOTE -> "Short, friendly summary for quick submission."
    }
}

fun buildToneGuideForRating(rating: Int): ReviewToneGuide {
    return when (rating) {
        5 -> ReviewToneGuide(
            title = "Celebrate what went right",
            body = "Mention punctuality, comfort, smooth navigation, or a friendly conversation.",
            colorLabel = "Success"
        )
        4 -> ReviewToneGuide(
            title = "Highlight the strong parts",
            body = "A 4-star review is perfect when most of the trip felt right with one or two small issues.",
            colorLabel = "Positive"
        )
        3 -> ReviewToneGuide(
            title = "Be specific and fair",
            body = "Explain which part felt average so the feedback stays constructive.",
            colorLabel = "Balanced"
        )
        2, 1 -> ReviewToneGuide(
            title = "Describe the issue clearly",
            body = "Short, concrete details help support follow-up without turning the review into a rant.",
            colorLabel = "Attention"
        )
        else -> ReviewToneGuide(
            title = "Start with a star rating",
            body = "Select a rating first so the rest of the form can adapt around it.",
            colorLabel = "Neutral"
        )
    }
}

fun buildSubmissionHeadline(rating: Int, anonymous: Boolean): String {
    val prefix = if (anonymous) "Anonymous" else "Public"
    return "$prefix ${rating}-star review"
}

fun ReviewSubmission.feedbackBalanceLabel(): String {
    return when {
        rating >= 5 -> "Excellent"
        rating == 4 -> "Very good"
        rating == 3 -> "Mixed"
        rating == 2 -> "Needs work"
        else -> "Critical"
    }
}

fun ReviewSubmission.helpfulScoreLabel(): String {
    return when {
        helpfulVotes >= 24 -> "Trending"
        helpfulVotes >= 10 -> "Helpful"
        helpfulVotes >= 1 -> "Getting noticed"
        else -> "New"
    }
}

fun formatShortDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
    return formatter.format(Date(timeMillis))
}
