package com.wheels.app.features.reviews.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerEvent
import com.wheels.app.features.reviews.domain.model.ReviewComposerTab
import com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerUiState

@Composable
fun ReviewComposerScreen(
    state: ReviewComposerUiState,
    onEvent: (ReviewComposerEvent) -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                ReviewHeroHeader(
                    state = state,
                    onEvent = onEvent
                )
            }

            item {
                ReviewTabRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onEvent(ReviewComposerEvent.TabChanged(it)) }
                )
            }

            item {
                when (state.selectedTab) {
                    ReviewComposerTab.WRITE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ReviewTripCarousel(
                                trips = state.availableTrips,
                                selectedTripId = state.selectedTrip?.id.orEmpty(),
                                onTripSelected = { onEvent(ReviewComposerEvent.TripSelected(it)) }
                            )

                            ReviewChecklistCard(
                                items = state.checklist
                            )

                            ReviewRatingCard(
                                rating = state.draft.rating,
                                toneGuide = state.toneGuide,
                                completionMessage = state.completionMessage,
                                onRatingChanged = { onEvent(ReviewComposerEvent.RatingChanged(it)) }
                            )

                            ReviewTemplateRail(
                                onTemplateSelected = { onEvent(ReviewComposerEvent.TemplateApplied(it)) }
                            )

                            ReviewAspectPicker(
                                selectedAspectIds = state.draft.selectedAspectIds,
                                onAspectToggled = { onEvent(ReviewComposerEvent.AspectToggled(it)) }
                            )

                            ReviewTagPicker(
                                selectedTagIds = state.draft.selectedTagIds,
                                onTagToggled = { onEvent(ReviewComposerEvent.TagToggled(it)) }
                            )

                            ReviewEditorCard(
                                comment = state.draft.comment,
                                privateNote = state.draft.privateNote,
                                visibilityLabel = state.draft.visibility.label,
                                audienceLabel = state.draft.audience.label,
                                isAnonymous = state.draft.isAnonymous,
                                followUpRequested = state.draft.followUpRequested,
                                helperMessage = state.helperMessage,
                                draftSummary = state.draftSummary,
                                lastSavedLabel = state.lastSavedLabel,
                                onCommentChanged = { onEvent(ReviewComposerEvent.CommentChanged(it)) },
                                onPrivateNoteChanged = { onEvent(ReviewComposerEvent.PrivateNoteChanged(it)) },
                                onAnonymousChanged = { onEvent(ReviewComposerEvent.AnonymousChanged(it)) },
                                onFollowUpChanged = { onEvent(ReviewComposerEvent.FollowUpChanged(it)) }
                            )

                            ReviewVisibilityCard(
                                visibility = state.draft.visibility,
                                audience = state.draft.audience,
                                onVisibilityChanged = { onEvent(ReviewComposerEvent.VisibilityChanged(it)) },
                                onAudienceChanged = { onEvent(ReviewComposerEvent.AudienceChanged(it)) }
                            )

                            ReviewMetricsCard(
                                metrics = state.metrics
                            )

                            ReviewInsightsCard(
                                insights = state.insights
                            )
                        }
                    }
                    ReviewComposerTab.HISTORY -> {
                        ReviewHistoryTab(
                            state = state,
                            onEvent = onEvent
                        )
                    }
                    ReviewComposerTab.INSIGHTS -> {
                        ReviewInsightsTab(
                            state = state
                        )
                    }
                }
            }
        }

        ReviewSubmitBar(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .navigationBarsPadding(),
            canSubmit = state.canSubmit,
            isSubmitting = state.isSubmitting,
            helperMessage = state.helperMessage,
            successMessage = state.successMessage,
            onSaveDraft = { onEvent(ReviewComposerEvent.SaveDraft) },
            onSubmit = { onEvent(ReviewComposerEvent.SubmitDraft) },
            onDiscard = { onEvent(ReviewComposerEvent.DiscardDraft) },
            onClearMessages = { onEvent(ReviewComposerEvent.ClearMessages) }
        )
    }
}

@Composable
private fun ReviewHistoryTab(
    state: ReviewComposerUiState,
    onEvent: (ReviewComposerEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReviewHistoryHeader(
            query = state.historySearchQuery,
            filter = state.historyFilter,
            onQueryChanged = { onEvent(ReviewComposerEvent.SearchChanged(it)) },
            onFilterSelected = { onEvent(ReviewComposerEvent.FilterChanged(it)) }
        )

        if (state.filteredSubmissions.isEmpty()) {
            ReviewEmptyHistoryCard(
                title = state.copy.emptyHistoryTitle,
                subtitle = state.copy.emptyHistorySubtitle
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.filteredSubmissions.forEach { submission ->
                ReviewSubmissionCard(submission = submission)
                }
            }
        }

        ReviewHistorySummaryCard(
            submissionsCount = state.submissions.size,
            visibleCount = state.filteredSubmissions.size,
            lastSavedLabel = state.lastSavedLabel
        )
    }
}

@Composable
private fun ReviewInsightsTab(
    state: ReviewComposerUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReviewInsightsHero(
            title = state.copy.title,
            subtitle = state.copy.subtitle
        )

        ReviewInsightsCard(
            insights = state.insights
        )

        ReviewMetricsCard(
            metrics = state.metrics
        )

        ReviewChecklistCard(
            items = state.checklist
        )

        ReviewToneGuideCard(
            toneGuide = state.toneGuide,
            completionMessage = state.completionMessage
        )
    }
}
