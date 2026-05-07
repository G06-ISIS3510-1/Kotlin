package com.wheels.app.features.reviews.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.Warning
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.reviews.domain.model.ReviewAspectDefinition
import com.wheels.app.features.reviews.domain.model.ReviewAudience
import com.wheels.app.features.reviews.domain.model.ReviewComposerCopy
import com.wheels.app.features.reviews.domain.model.ReviewComposerTab
import com.wheels.app.features.reviews.domain.model.ReviewChecklistItem
import com.wheels.app.features.reviews.domain.model.ReviewHistoryFilter
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewMetric
import com.wheels.app.features.reviews.domain.model.ReviewSubmission
import com.wheels.app.features.reviews.domain.model.ReviewTagDefinition
import com.wheels.app.features.reviews.domain.model.ReviewToneGuide
import com.wheels.app.features.reviews.domain.model.ReviewVisibility
import com.wheels.app.features.reviews.domain.model.ReviewWritingTemplate
import com.wheels.app.features.reviews.domain.model.TripContext
import com.wheels.app.features.reviews.presentation.mock.ReviewComposerPreviewData
import androidx.compose.material3.TextButton

@Composable
fun ReviewHeroHeader(
    state: com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerUiState,
    onEvent: (com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF1A3A5C),
                        Color(0xFF2563EB),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            text = state.copy.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = state.copy.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            color = WheelsSurface.copy(alpha = 0.12f),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WheelsSurface.copy(alpha = 0.20f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = ElectricGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.draftSummary.ifBlank { "No draft yet" },
                        color = WheelsSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = state.lastSavedLabel,
                        color = WheelsSurface.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = { onEvent(com.wheels.app.features.reviews.presentation.viewmodel.ReviewComposerEvent.SaveDraft) }) {
                    Text(text = "Save", color = WheelsSurface)
                }
            }
        }
    }
}

@Composable
fun ReviewTabRow(
    selectedTab: ReviewComposerTab,
    onTabSelected: (ReviewComposerTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReviewComposerTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) PrimaryBlue else WheelsSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                modifier = Modifier.clickable { onTabSelected(tab) }
            ) {
                Text(
                    text = tab.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (selected) WheelsSurface else PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun ReviewTripCarousel(
    trips: List<TripContext>,
    selectedTripId: String,
    onTripSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Completed trips")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(trips, key = { it.id }) { trip ->
                ReviewTripCard(
                    trip = trip,
                    selected = trip.id == selectedTripId,
                    onClick = { onTripSelected(trip.id) }
                )
            }
        }
    }
}

@Composable
private fun ReviewTripCard(
    trip: TripContext,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE8F0F9) else WheelsSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PrimaryBlue else Border
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 10.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trip.driverInitials,
                        color = WheelsSurface,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.driverName,
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = trip.vehicleLabel,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (trip.verifiedDriver) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Border)

            TripInfoLine(label = "Route", value = trip.routeName)
            TripInfoLine(label = "Date", value = trip.dateLabel)
            TripInfoLine(label = "Time", value = trip.timeLabel)

            Surface(
                color = Color(0xFFE8F0F9),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = trip.badgeLabel,
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewChecklistCard(
    items: List<ReviewChecklistItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Review checklist")
            items.forEach { item ->
                ChecklistItemRow(item = item)
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(item: ReviewChecklistItem) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (item.done) ElectricGreen.copy(alpha = 0.12f) else Border),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.done) Icons.Outlined.CheckCircle else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (item.done) ElectricGreen else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = item.title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = item.body,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ReviewRatingCard(
    rating: Int,
    toneGuide: ReviewToneGuide,
    completionMessage: String,
    onRatingChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Rate the ride")

            Text(
                text = "Tap the star that matches the trip. The text below adapts to the score you choose.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { value ->
                    val selected = value <= rating
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Color(0xFFFFF3D6) else Color(0xFFE8F0F9))
                            .border(
                                width = 1.dp,
                                color = if (selected) Warning else Border,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onRatingChanged(value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (selected) Warning else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Surface(
                color = Color(0xFFE8F0F9),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = toneGuide.title,
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = toneGuide.body,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = completionMessage,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewTemplateRail(
    onTemplateSelected: (ReviewWritingTemplate) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Quick templates")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReviewWritingTemplate.entries, key = { it.name }) { template ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = WheelsSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.clickable { onTemplateSelected(template) }
                ) {
                    Text(
                        text = template.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewAspectPicker(
    selectedAspectIds: Set<String>,
    onAspectToggled: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Feedback aspects")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReviewComposerPreviewData.aspects, key = { it.id }) { aspect ->
                ReviewChip(
                    title = aspect.title,
                    subtitle = aspect.description,
                    selected = aspect.id in selectedAspectIds,
                    accentColor = Color(aspect.highlightColorHex),
                    onClick = { onAspectToggled(aspect.id) }
                )
            }
        }
    }
}

@Composable
fun ReviewTagPicker(
    selectedTagIds: Set<String>,
    onTagToggled: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Suggested tags")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReviewComposerPreviewData.tags, key = { it.id }) { tag ->
                val selected = tag.id in selectedTagIds
                ReviewTagChip(
                    tag = tag,
                    selected = selected,
                    onClick = { onTagToggled(tag.id) }
                )
            }
        }
    }
}

@Composable
private fun ReviewTagChip(
    tag: ReviewTagDefinition,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFFE8F0F9) else WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) SecondaryBlue else Border),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = tag.label,
                color = if (selected) SecondaryBlue else PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = tag.helperText,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ReviewChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accentColor.copy(alpha = 0.12f) else WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) accentColor else Border),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ReviewEditorCard(
    comment: String,
    privateNote: String,
    visibilityLabel: String,
    audienceLabel: String,
    isAnonymous: Boolean,
    followUpRequested: Boolean,
    helperMessage: String?,
    draftSummary: String,
    lastSavedLabel: String,
    onCommentChanged: (String) -> Unit,
    onPrivateNoteChanged: (String) -> Unit,
    onAnonymousChanged: (Boolean) -> Unit,
    onFollowUpChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Write your review")

            if (helperMessage != null) {
                Surface(
                    color = Color(0xFFE8F0F9),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = helperMessage,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comment") },
                placeholder = { Text("Tell the next person what mattered most.") },
                minLines = 4
            )

            OutlinedTextField(
                value = privateNote,
                onValueChange = onPrivateNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Private note") },
                placeholder = { Text("Keep a note for yourself or future follow-up.") },
                minLines = 2
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniInfoPill(label = "Audience", value = audienceLabel, modifier = Modifier.weight(1f))
                MiniInfoPill(label = "Visibility", value = visibilityLabel, modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Anonymous review",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Hide your name from the displayed submission.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = isAnonymous, onCheckedChange = onAnonymousChanged)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Request follow up",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Mark this when the situation may need attention later.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = followUpRequested, onCheckedChange = onFollowUpChanged)
            }

            Surface(
                color = Color(0xFFE8F0F9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Draft summary",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(text = draftSummary, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = lastSavedLabel, color = SecondaryBlue, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun ReviewVisibilityCard(
    visibility: ReviewVisibility,
    audience: ReviewAudience,
    onVisibilityChanged: (ReviewVisibility) -> Unit,
    onAudienceChanged: (ReviewAudience) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Sharing controls")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReviewAudience.entries.forEach { option ->
                    ReviewChoicePill(
                        label = option.label,
                        selected = option == audience,
                        onClick = { onAudienceChanged(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReviewVisibility.entries.forEach { option ->
                    ReviewChoicePill(
                        label = option.label,
                        selected = option == visibility,
                        onClick = { onVisibilityChanged(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) PrimaryBlue else WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) PrimaryBlue else Border)
    ) {
        Text(
            text = label,
            color = if (selected) WheelsSurface else PrimaryBlue,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReviewMetricsCard(metrics: List<ReviewMetric>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Review stats")
            metrics.forEachIndexed { index, metric ->
                MetricRow(metric = metric)
                if (index != metrics.lastIndex) {
                    HorizontalDivider(color = Border)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(metric: ReviewMetric) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric.title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = metric.hint,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = metric.value,
            color = SecondaryBlue,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ReviewInsightsCard(insights: List<ReviewInsight>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Review insights")
            insights.forEach { insight ->
                InsightRow(insight = insight)
            }
        }
    }
}

@Composable
private fun InsightRow(insight: ReviewInsight) {
    Surface(
        color = if (insight.positiveTrend) ElectricGreen.copy(alpha = 0.10f) else Warning.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (insight.positiveTrend) ElectricGreen.copy(alpha = 0.20f) else Warning.copy(alpha = 0.20f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (insight.positiveTrend) ElectricGreen.copy(alpha = 0.16f) else Warning.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (insight.positiveTrend) Icons.Outlined.CheckCircle else Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = if (insight.positiveTrend) ElectricGreen else Warning,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = insight.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = insight.value,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = insight.trendLabel,
                    color = if (insight.positiveTrend) ElectricGreen else Warning,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun ReviewToneGuideCard(
    toneGuide: ReviewToneGuide,
    completionMessage: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Tone guide")
            Text(
                text = toneGuide.title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = toneGuide.body,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Surface(
                color = Color(0xFFE8F0F9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = completionMessage,
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewHistoryHeader(
    query: String,
    filter: ReviewHistoryFilter,
    onQueryChanged: (String) -> Unit,
    onFilterSelected: (ReviewHistoryFilter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("History filters")
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search history") },
                placeholder = { Text("Driver, route, or note") }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReviewHistoryFilter.entries, key = { it.name }) { entry ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (entry == filter) PrimaryBlue else WheelsSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier.clickable { onFilterSelected(entry) }
                    ) {
                        Text(
                            text = entry.label,
                            color = if (entry == filter) WheelsSurface else PrimaryBlue,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSubmissionCard(submission: ReviewSubmission) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = submission.driverInitials,
                        color = WheelsSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.driverName,
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = submission.tripRouteLabel,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < submission.rating) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(
                text = submission.commentPreview,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE8F0F9)) {
                    Text(
                        text = submission.audience.label,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE8F0F9)) {
                    Text(
                        text = submission.visibility.label,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE8F0F9)) {
                    Text(
                        text = submission.submittedAtLabel,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${submission.helpfulVotes} helpful votes",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Reply count ${submission.replyCount}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun ReviewHistorySummaryCard(
    submissionsCount: Int,
    visibleCount: Int,
    lastSavedLabel: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "History summary",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "$visibleCount of $submissionsCount reviews visible with the current filter.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last updated $lastSavedLabel",
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun ReviewEmptyHistoryCard(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = null,
                tint = SecondaryBlue,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ReviewInsightsHero(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = WheelsSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReviewSubmitBar(
    canSubmit: Boolean,
    isSubmitting: Boolean,
    helperMessage: String?,
    successMessage: String?,
    onSaveDraft: () -> Unit,
    onSubmit: () -> Unit,
    onDiscard: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WheelsSurface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (successMessage != null) {
                Surface(
                    color = ElectricGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable(onClick = onClearMessages)
                ) {
                    Text(
                        text = successMessage,
                        color = ElectricGreen,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (helperMessage != null) {
                Surface(
                    color = Color(0xFFE8F0F9),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = helperMessage,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewActionButton(
                    text = "Save draft",
                    icon = Icons.Outlined.AccessTime,
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f)
                )
                ReviewActionButton(
                    text = if (isSubmitting) "Submitting..." else "Submit review",
                    icon = Icons.Outlined.CheckCircle,
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    modifier = Modifier.weight(1f)
                )
            }

            ReviewActionButton(
                text = "Discard draft",
                icon = Icons.Outlined.ChevronRight,
                onClick = onDiscard,
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReviewActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) PrimaryBlue else Border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WheelsSurface, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = WheelsSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun MiniInfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F0F9)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun TripInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(text = value, color = PrimaryBlue, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
    }
}
