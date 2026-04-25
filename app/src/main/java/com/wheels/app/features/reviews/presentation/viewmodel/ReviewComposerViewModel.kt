package com.wheels.app.features.reviews.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheels.app.features.reviews.data.repository.InMemoryReviewComposerRepository
import com.wheels.app.features.reviews.domain.model.ReviewChecklistItem
import com.wheels.app.features.reviews.domain.model.ReviewComposerCopy
import com.wheels.app.features.reviews.domain.model.ReviewComposerTab
import com.wheels.app.features.reviews.domain.model.ReviewAudience
import com.wheels.app.features.reviews.domain.model.ReviewDraft
import com.wheels.app.features.reviews.domain.model.ReviewHistoryFilter
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewMetric
import com.wheels.app.features.reviews.domain.model.ReviewToneGuide
import com.wheels.app.features.reviews.domain.model.ReviewVisibility
import com.wheels.app.features.reviews.domain.model.ReviewWritingTemplate
import com.wheels.app.features.reviews.domain.model.TripContext
import com.wheels.app.features.reviews.domain.model.buildBlankDraft
import com.wheels.app.features.reviews.domain.model.buildReviewChecklist
import com.wheels.app.features.reviews.domain.model.buildSubmissionHeadline
import com.wheels.app.features.reviews.domain.model.buildTemplateComment
import com.wheels.app.features.reviews.domain.model.buildTemplatePrivateNote
import com.wheels.app.features.reviews.domain.model.buildToneGuideForRating
import com.wheels.app.features.reviews.domain.model.reviewCompletionMessage
import com.wheels.app.features.reviews.domain.model.previewSummary
import com.wheels.app.features.reviews.domain.model.toggleAspect
import com.wheels.app.features.reviews.domain.model.toggleTag
import com.wheels.app.features.reviews.domain.model.withAnonymous
import com.wheels.app.features.reviews.domain.model.withAudience
import com.wheels.app.features.reviews.domain.model.withComment
import com.wheels.app.features.reviews.domain.model.withFollowUpRequested
import com.wheels.app.features.reviews.domain.model.withPrivateNote
import com.wheels.app.features.reviews.domain.model.withRating
import com.wheels.app.features.reviews.domain.model.withVisibility
import com.wheels.app.features.reviews.domain.repository.ReviewComposerRepository
import com.wheels.app.features.reviews.domain.usecase.DiscardReviewDraftUseCase
import com.wheels.app.features.reviews.domain.usecase.ObserveAvailableTripsUseCase
import com.wheels.app.features.reviews.domain.usecase.ObserveCurrentDraftUseCase
import com.wheels.app.features.reviews.domain.usecase.ObserveReviewHistoryUseCase
import com.wheels.app.features.reviews.domain.usecase.ObserveReviewInsightsUseCase
import com.wheels.app.features.reviews.domain.usecase.SaveReviewDraftUseCase
import com.wheels.app.features.reviews.domain.usecase.SeedReviewsFromLastTripUseCase
import com.wheels.app.features.reviews.domain.usecase.SetActiveTripUseCase
import com.wheels.app.features.reviews.domain.usecase.SubmitReviewDraftUseCase
import com.wheels.app.features.reviews.presentation.mock.ReviewComposerPreviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewComposerViewModel(
    repository: ReviewComposerRepository = InMemoryReviewComposerRepository()
) : ViewModel() {

    private val observeTripsUseCase = ObserveAvailableTripsUseCase(repository)
    private val observeDraftUseCase = ObserveCurrentDraftUseCase(repository)
    private val observeHistoryUseCase = ObserveReviewHistoryUseCase(repository)
    private val observeInsightsUseCase = ObserveReviewInsightsUseCase(repository)
    private val saveReviewDraftUseCase = SaveReviewDraftUseCase(repository)
    private val submitReviewDraftUseCase = SubmitReviewDraftUseCase(repository)
    private val discardReviewDraftUseCase = DiscardReviewDraftUseCase(repository)
    private val setActiveTripUseCase = SetActiveTripUseCase(repository)
    private val seedReviewsFromLastTripUseCase = SeedReviewsFromLastTripUseCase(repository)

    private val _uiState = MutableStateFlow(
        ReviewComposerUiState(
            copy = ReviewComposerPreviewData.copy
        )
    )
    val uiState: StateFlow<ReviewComposerUiState> = _uiState.asStateFlow()

    init {
        observeState()
        viewModelScope.launch {
            seedReviewsFromLastTripUseCase()
        }
    }

    fun onEvent(event: ReviewComposerEvent) {
        when (event) {
            is ReviewComposerEvent.TabChanged -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is ReviewComposerEvent.TripSelected -> selectTrip(event.tripId)
            is ReviewComposerEvent.RatingChanged -> updateDraft { withRating(event.rating) }
            is ReviewComposerEvent.AudienceChanged -> updateDraft { withAudience(event.audience) }
            is ReviewComposerEvent.VisibilityChanged -> updateDraft { withVisibility(event.visibility) }
            is ReviewComposerEvent.AspectToggled -> updateDraft { toggleAspect(event.aspectId) }
            is ReviewComposerEvent.TagToggled -> updateDraft { toggleTag(event.tagId) }
            is ReviewComposerEvent.CommentChanged -> updateDraft { withComment(event.value) }
            is ReviewComposerEvent.PrivateNoteChanged -> updateDraft { withPrivateNote(event.value) }
            is ReviewComposerEvent.AnonymousChanged -> updateDraft { withAnonymous(event.value) }
            is ReviewComposerEvent.FollowUpChanged -> updateDraft { withFollowUpRequested(event.value) }
            is ReviewComposerEvent.TemplateApplied -> applyTemplate(event.template)
            ReviewComposerEvent.SaveDraft -> persistDraft()
            ReviewComposerEvent.SubmitDraft -> submitDraft()
            is ReviewComposerEvent.SearchChanged -> {
                _uiState.update { it.copy(historySearchQuery = event.value) }
                rebuildFilteredHistory()
            }
            is ReviewComposerEvent.FilterChanged -> {
                _uiState.update { it.copy(historyFilter = event.filter) }
                rebuildFilteredHistory()
            }
            ReviewComposerEvent.ClearMessages -> _uiState.update {
                it.copy(helperMessage = null, successMessage = null)
            }
            ReviewComposerEvent.DiscardDraft -> discardDraft()
            ReviewComposerEvent.ResetForm -> resetCurrentDraft()
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                observeTripsUseCase().catch { emit(emptyList()) },
                observeDraftUseCase().catch { emit(null) },
                observeHistoryUseCase().catch { emit(emptyList()) },
                observeInsightsUseCase().catch { emit(emptyList()) }
            ) { trips, draft, history, insights ->
                QuadState(trips, draft, history, insights)
            }.collect { state ->
                _uiState.update { current ->
                    val activeDraft = state.draft ?: current.draft
                    val selectedTrip = resolveSelectedTrip(state.trips, activeDraft)
                    val checklist = buildReviewChecklist(activeDraft)
                    val toneGuide = buildToneGuideForRating(activeDraft.rating)
                    val metrics = buildMetrics(activeDraft, state.history, selectedTrip)
                    val filteredHistory = filterHistory(
                        submissions = state.history,
                        query = current.historySearchQuery,
                        filter = current.historyFilter
                    )

                    current.copy(
                        isLoading = false,
                        availableTrips = state.trips,
                        selectedTrip = selectedTrip,
                        draft = activeDraft,
                        submissions = state.history,
                        insights = if (state.insights.isNotEmpty()) state.insights else buildFallbackInsights(state.history),
                        checklist = checklist,
                        metrics = metrics,
                        toneGuide = toneGuide,
                        filteredSubmissions = filteredHistory,
                        draftSummary = activeDraft.previewSummary(),
                        completionMessage = reviewCompletionMessage(activeDraft.rating, activeDraft.audience),
                        activeHeadline = if (activeDraft.hasRating) buildSubmissionHeadline(activeDraft.rating, activeDraft.isAnonymous) else "Waiting for rating",
                        canSubmit = activeDraft.canSubmit && !current.isSubmitting,
                        helperMessage = current.helperMessage ?: buildHelperMessage(activeDraft, selectedTrip),
                        lastSavedLabel = activeDraft.updatedAtLabel
                    )
                }
            }
        }
    }

    private fun selectTrip(tripId: String) {
        viewModelScope.launch {
            setActiveTripUseCase(tripId)
            _uiState.update { current ->
                val trip = current.availableTrips.firstOrNull { it.id == tripId }
                val draft = trip?.let { current.draft.copy(tripId = it.id) } ?: current.draft
                current.copy(
                    selectedTrip = trip,
                    draft = draft,
                    helperMessage = trip?.let { "Reviewing ${it.routeName} with ${it.driverName}." } ?: current.helperMessage
                )
            }
            persistDraft()
        }
    }

    private fun updateDraft(transform: ReviewDraft.() -> ReviewDraft) {
        val currentDraft = _uiState.value.draft
        val updated = currentDraft.transform().copy(updatedAtLabel = "Just now")
        _uiState.update {
            it.copy(
                draft = updated,
                checklist = buildReviewChecklist(updated),
                toneGuide = buildToneGuideForRating(updated.rating),
                draftSummary = updated.previewSummary(),
                completionMessage = reviewCompletionMessage(updated.rating, updated.audience),
                activeHeadline = if (updated.hasRating) buildSubmissionHeadline(updated.rating, updated.isAnonymous) else "Waiting for rating",
                canSubmit = updated.canSubmit && !it.isSubmitting,
                helperMessage = when {
                    updated.commentLength < 20 -> "Add a few more details so the review is useful to the next person."
                    updated.selectedTagIds.isEmpty() -> "Pick a couple of tags to make the review easier to scan."
                    else -> "Draft updated locally. You can submit whenever it feels ready."
                },
                lastSavedLabel = "Just now"
            )
        }
        viewModelScope.launch { saveReviewDraftUseCase(updated) }
        rebuildFilteredHistory()
    }

    private fun applyTemplate(template: ReviewWritingTemplate) {
        val trip = _uiState.value.selectedTrip
        val draft = _uiState.value.draft.copy(
            comment = buildTemplateComment(template, trip),
            privateNote = buildTemplatePrivateNote(template),
            rating = when (template) {
                ReviewWritingTemplate.PRAISE -> 5
                ReviewWritingTemplate.BALANCED -> 4
                ReviewWritingTemplate.CONSTRUCTIVE -> 3
                ReviewWritingTemplate.ISSUE_REPORT -> 2
                ReviewWritingTemplate.SHORT_NOTE -> 4
            },
            selectedAspectIds = when (template) {
                ReviewWritingTemplate.PRAISE -> setOf("punctuality", "communication", "safety")
                ReviewWritingTemplate.BALANCED -> setOf("communication", "route")
                ReviewWritingTemplate.CONSTRUCTIVE -> setOf("punctuality", "route")
                ReviewWritingTemplate.ISSUE_REPORT -> setOf("punctuality", "communication")
                ReviewWritingTemplate.SHORT_NOTE -> setOf("respect", "comfort")
            },
            selectedTagIds = when (template) {
                ReviewWritingTemplate.PRAISE -> setOf("on_time", "friendly", "safe_drive")
                ReviewWritingTemplate.BALANCED -> setOf("comfortable", "good_chat")
                ReviewWritingTemplate.CONSTRUCTIVE -> setOf("late_notice", "route_issue")
                ReviewWritingTemplate.ISSUE_REPORT -> setOf("needs_followup", "late_notice")
                ReviewWritingTemplate.SHORT_NOTE -> setOf("clean_car", "quiet_ride")
            },
            audience = if (template == ReviewWritingTemplate.ISSUE_REPORT) ReviewAudience.DRIVER else ReviewAudience.DRIVER,
            visibility = if (template == ReviewWritingTemplate.ISSUE_REPORT) ReviewVisibility.PRIVATE else ReviewVisibility.PUBLIC
        )
        _uiState.update {
            it.copy(
                draft = draft,
                checklist = buildReviewChecklist(draft),
                toneGuide = buildToneGuideForRating(draft.rating),
                draftSummary = draft.previewSummary(),
                completionMessage = reviewCompletionMessage(draft.rating, draft.audience),
                activeHeadline = buildSubmissionHeadline(draft.rating, draft.isAnonymous),
                helperMessage = "Applied the ${template.label.lowercase()} template."
            )
        }
        viewModelScope.launch { saveReviewDraftUseCase(draft) }
        rebuildFilteredHistory()
    }

    private fun persistDraft() {
        val draft = _uiState.value.draft.copy(updatedAtLabel = "Just now")
        viewModelScope.launch {
            saveReviewDraftUseCase(draft)
        }
        _uiState.update {
            it.copy(
                draft = draft,
                helperMessage = "Draft saved locally."
            )
        }
    }

    private fun submitDraft() {
        val state = _uiState.value
        if (!state.draft.canSubmit) {
            _uiState.update { it.copy(helperMessage = "Add a star rating and a slightly longer comment before submitting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, helperMessage = "Submitting review...") }
            val result = submitReviewDraftUseCase()
            if (result == null) {
                _uiState.update { it.copy(isSubmitting = false, helperMessage = "No draft was ready to submit.") }
                return@launch
            }

            _uiState.update { current ->
                val nextDraft = buildBlankDraft(current.selectedTrip?.id ?: result.tripId)
                current.copy(
                    isSubmitting = false,
                    successMessage = "Review submitted successfully.",
                    helperMessage = "The review for ${result.tripRouteLabel} is now in history.",
                    draft = nextDraft,
                    checklist = buildReviewChecklist(nextDraft),
                    toneGuide = buildToneGuideForRating(nextDraft.rating),
                    draftSummary = nextDraft.previewSummary(),
                    activeHeadline = "Waiting for rating",
                    canSubmit = false
                )
            }
        }
    }

    private fun discardDraft() {
        val tripId = _uiState.value.draft.tripId.ifBlank { _uiState.value.selectedTrip?.id.orEmpty() }
        if (tripId.isBlank()) return

        viewModelScope.launch {
            discardReviewDraftUseCase(tripId)
            _uiState.update { current ->
                val reset = buildBlankDraft(tripId)
                current.copy(
                    draft = reset,
                    checklist = buildReviewChecklist(reset),
                    toneGuide = buildToneGuideForRating(reset.rating),
                    draftSummary = reset.previewSummary(),
                    helperMessage = "Draft discarded and reset.",
                    activeHeadline = "Waiting for rating",
                    canSubmit = false
                )
            }
            rebuildFilteredHistory()
        }
    }

    private fun resetCurrentDraft() {
        val tripId = _uiState.value.selectedTrip?.id ?: _uiState.value.draft.tripId
        val reset = buildBlankDraft(tripId)
        _uiState.update {
            it.copy(
                draft = reset,
                checklist = buildReviewChecklist(reset),
                toneGuide = buildToneGuideForRating(reset.rating),
                draftSummary = reset.previewSummary(),
                helperMessage = "Form cleared.",
                activeHeadline = "Waiting for rating",
                canSubmit = false
            )
        }
        viewModelScope.launch { saveReviewDraftUseCase(reset) }
    }

    private fun rebuildFilteredHistory() {
        _uiState.update { current ->
            current.copy(
                filteredSubmissions = filterHistory(
                    submissions = current.submissions,
                    query = current.historySearchQuery,
                    filter = current.historyFilter
                )
            )
        }
    }

    private fun buildMetrics(
        draft: ReviewDraft,
        history: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission>,
        selectedTrip: TripContext?
    ): List<ReviewMetric> {
        val tagCount = draft.selectedTagIds.size
        val aspectCount = draft.selectedAspectIds.size
        val visibleHistory = filterHistory(history, "", ReviewHistoryFilter.PUBLIC)
        return listOf(
            ReviewMetric("Selected tags", tagCount.toString(), "Quick labels for the review"),
            ReviewMetric("Selected aspects", aspectCount.toString(), "What this feedback focuses on"),
            ReviewMetric("Public reviews", visibleHistory.size.toString(), "Already visible in the history"),
            ReviewMetric("Current route", selectedTrip?.routeName ?: "--", "What the review is attached to")
        )
    }

    private fun buildFallbackInsights(history: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission>): List<ReviewInsight> {
        val avg = if (history.isEmpty()) 0.0 else history.map { it.rating }.average()
        val count = history.size
        return listOf(
            ReviewInsight(
                title = "Local average",
                value = if (count == 0) "--" else String.format("%.1f", avg),
                subtitle = "Derived directly from the isolated repository state.",
                trendLabel = if (avg >= 4.0) "Healthy" else "Build more data",
                positiveTrend = avg >= 4.0
            )
        )
    }

    private fun resolveSelectedTrip(trips: List<TripContext>, draft: ReviewDraft): TripContext? {
        return trips.firstOrNull { it.id == draft.tripId } ?: trips.firstOrNull()
    }

    private fun buildHelperMessage(draft: ReviewDraft, trip: TripContext?): String? {
        if (trip == null) return "Choose a completed trip to start your review."
        return when {
            draft.rating == 0 -> "Start by rating the trip from 1 to 5 stars."
            draft.commentLength < 20 -> "Add a few details so the comment is helpful to future riders."
            draft.visibility == ReviewVisibility.PRIVATE -> "Private reviews stay in this isolated draft until you connect it later."
            else -> "This draft is ready when you are."
        }
    }

    private fun filterHistory(
        submissions: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission>,
        query: String,
        filter: ReviewHistoryFilter
    ): List<com.wheels.app.features.reviews.domain.model.ReviewSubmission> {
        val normalizedQuery = query.trim().lowercase()
        return submissions.filter { submission ->
            val matchesQuery = normalizedQuery.isBlank() ||
                submission.comment.lowercase().contains(normalizedQuery) ||
                submission.driverName.lowercase().contains(normalizedQuery) ||
                submission.tripRouteLabel.lowercase().contains(normalizedQuery)

            val matchesFilter = when (filter) {
                ReviewHistoryFilter.ALL -> true
                ReviewHistoryFilter.FIVE_STAR -> submission.rating == 5
                ReviewHistoryFilter.PUBLIC -> submission.visibility == ReviewVisibility.PUBLIC
                ReviewHistoryFilter.ANONYMOUS -> submission.isAnonymous
                ReviewHistoryFilter.NEEDS_ATTENTION -> submission.rating <= 3
            }

            matchesQuery && matchesFilter
        }
    }

    private data class QuadState(
        val trips: List<TripContext>,
        val draft: ReviewDraft?,
        val history: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission>,
        val insights: List<ReviewInsight>
    )
}

sealed interface ReviewComposerEvent {
    data class TabChanged(val tab: ReviewComposerTab) : ReviewComposerEvent
    data class TripSelected(val tripId: String) : ReviewComposerEvent
    data class RatingChanged(val rating: Int) : ReviewComposerEvent
    data class AudienceChanged(val audience: com.wheels.app.features.reviews.domain.model.ReviewAudience) : ReviewComposerEvent
    data class VisibilityChanged(val visibility: ReviewVisibility) : ReviewComposerEvent
    data class AspectToggled(val aspectId: String) : ReviewComposerEvent
    data class TagToggled(val tagId: String) : ReviewComposerEvent
    data class CommentChanged(val value: String) : ReviewComposerEvent
    data class PrivateNoteChanged(val value: String) : ReviewComposerEvent
    data class AnonymousChanged(val value: Boolean) : ReviewComposerEvent
    data class FollowUpChanged(val value: Boolean) : ReviewComposerEvent
    data class TemplateApplied(val template: ReviewWritingTemplate) : ReviewComposerEvent
    data class SearchChanged(val value: String) : ReviewComposerEvent
    data class FilterChanged(val filter: ReviewHistoryFilter) : ReviewComposerEvent
    data object SaveDraft : ReviewComposerEvent
    data object SubmitDraft : ReviewComposerEvent
    data object DiscardDraft : ReviewComposerEvent
    data object ResetForm : ReviewComposerEvent
    data object ClearMessages : ReviewComposerEvent
}

data class ReviewComposerUiState(
    val isLoading: Boolean = true,
    val availableTrips: List<TripContext> = emptyList(),
    val selectedTrip: TripContext? = null,
    val draft: ReviewDraft = ReviewDraft(),
    val submissions: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission> = emptyList(),
    val filteredSubmissions: List<com.wheels.app.features.reviews.domain.model.ReviewSubmission> = emptyList(),
    val insights: List<ReviewInsight> = emptyList(),
    val checklist: List<ReviewChecklistItem> = emptyList(),
    val metrics: List<ReviewMetric> = emptyList(),
    val toneGuide: ReviewToneGuide = buildToneGuideForRating(0),
    val copy: ReviewComposerCopy = ReviewComposerPreviewData.copy,
    val selectedTab: ReviewComposerTab = ReviewComposerTab.WRITE,
    val historyFilter: ReviewHistoryFilter = ReviewHistoryFilter.ALL,
    val historySearchQuery: String = "",
    val helperMessage: String? = null,
    val successMessage: String? = null,
    val draftSummary: String = "",
    val completionMessage: String = "",
    val activeHeadline: String = "",
    val lastSavedLabel: String = "Just now",
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false
)
