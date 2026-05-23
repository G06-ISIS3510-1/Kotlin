// This file is intentionally standalone.
// It explores a different visual direction for notifications without wiring anything
// into navigation, persistence, ViewModels, or the rest of the app shell.
// The palette is local on purpose so the layout can feel editorial and distinct
// from the default app surfaces while still remaining readable and calm.
private val LayoutBackground = Color(0xFF07111F)
/**
 * Standalone notifications layout concept.
 *
 * Design intent:
 * - Left side: a dense triage feed for scanning many notifications quickly.
 * - Right side: a slower, more readable detail panel for the selected item.
 * This composable is intentionally disconnected from the app flow so it can be used
 * as a design reference, preview target, or later integration point without hidden side effects.
 */
    // Resolve the selected notification once so both columns stay visually in sync.
    // If a selected ID is not found, the first available notification becomes the fallback.
    val selected = notifications.firstOrNull { it.id == selectedId } ?: notifications.firstOrNull()
    Surface(modifier = modifier.fillMaxSize(), color = LayoutBackground) {
        // The outer Box lets us paint the atmosphere behind the content without
        // complicating the feed and detail components with background concerns.
                // The header provides immediate context: what this screen is for,
                // how many notifications exist, and what the inbox state feels like.
                NotificationsLayoutHeader(totalCount = notifications.size)
                // The two-column split is the main design choice.
                // The inbox gets slightly more width because list scanning is the dominant task.
                Row(
                NotificationsLayoutFooterLedger(
                    selected = selected,
                    unreadCount = unreadCount,
/**
 * Header block for the layout.
 *
 * The header uses a title, short explanatory copy, and a few metric chips so the
 * screen can communicate its current state before the user scrolls the feed.
 */
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Notification Atlas",
        Text(
            text = "An alternative editorial layout for inbox scanning, detail reading, and future offline sync state.",
            color = LayoutMuted
/**
 * A light-weight toolbar that makes the layout feel more complete.
 *
 * The controls are intentionally cosmetic here. They describe the available inbox modes
 * without pretending the feature is already wired to state or navigation.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        LayoutMetric("Total", totalCount.toString())
        LayoutMetric("Unread", unreadCount.toString())
/**
 * Small summary chip used by the header.
 *
 * The chip is intentionally understated: it should add orientation, not compete with the
 * actual notification content.
 */
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutSurfaceAlt.copy(alpha = 0.82f)),
/**
 * A wider narrative strip that explains the inbox state in plain language.
 *
 * This section helps the screen feel like a dashboard instead of just a two-pane list.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutNarrativeCard(
            label = "Focus",
/**
 * Narrative card used inside the strip above.
 *
 * The card mixes a label, a value, and a short explanation so the top region
 * can communicate context even before the user looks at the list.
 */
    Card(
        modifier = Modifier.fillMaxWidth(0.31f),
        shape = RoundedCornerShape(22.dp),
/**
 * Inbox column.
 *
 * The feed is designed to be dense enough for triage but still visually separated item by item,
 * which matters when a user is quickly reviewing many notifications after being away from the app.
 */
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
/**
 * A compact row of filter chips that makes the feed feel more interactive.
 *
 * The chips are decorative for now, but the structure is ready for actual filter state later.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LayoutFilterChip("All $totalCount", selected = true)
        LayoutFilterChip("Unread $unreadCount", selected = false)
/**
 * A single notification preview item.
 *
 * The visual treatment changes slightly when selected so the user always has a clear anchor
 * between the list and the detail pane.
 */
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
/**
 * Footer row for each inbox item.
 *
 * It adds a little more structure around the timestamp and read state so the list can
 * communicate state at a glance without becoming visually busy.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${notification.timestampLabel} - ${if (notification.isRead) "read" else "unread"}",
/**
 * Tiny visual accent used in the item footer.
 *
 * A dot is enough here because it gives the layout a little rhythm without demanding attention.
 */
    Box(
        modifier = Modifier
            .width(8.dp)
/**
 * Feed footer that frames the inbox as a working queue.
 *
 * It adds one more summary layer so the feed has a clear end point and a clearer role.
 */
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LayoutMetric("Visible", totalCount.toString())
/**
 * Detail column.
 *
 * This panel is intentionally more spacious and more readable than the feed. That contrast
 * makes the layout feel like a workspace instead of a plain list.
 */
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
/**
 * Small detail summary cards that make the right column feel more editorial.
 *
 * The cards are read-only, but they hint at the kinds of metadata that would be surfaced
 * once the inbox is backed by a real repository and state source.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutMetric("Selected", notification?.id ?: "None")
        LayoutMetric("State", if (notification?.isRead == true) "Read" else "Pending")
/**
 * Action row that sketches future inbox operations.
 *
 * These are decorative placeholders for now, but they help tell the story of the feature:
 * notifications are not just viewed, they are managed.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutActionBadge("Mark read")
        LayoutActionBadge("Delete")
/**
 * Reusable action badge for the detail panel.
 *
 * The badge has the feel of a command chip rather than a destructive button because the
 * file is still a static concept rather than a live interaction layer.
 */
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutBackground.copy(alpha = 0.18f)),
/**
 * Supporting copy for the detail pane.
 *
 * The extra notes preserve the current story of the screen: local-first browsing, eventual
 * sync, and a clear separation between what is visible now and what will be wired later.
 */
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "The layout leaves space for future read-state, offline state, and queued actions without wiring them yet.",
/**
 * A small legend that explains the current state of the detail pane.
 *
 * These tags are deliberately lightweight so they work like labels, not buttons.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LayoutToolbarPill(if (notification == null) "Waiting" else "Loaded")
        LayoutToolbarPill(if (notification?.isRead == true) "Read" else "Unread")
/**
 * A related-items section that gives the detail pane more depth.
 *
 * The content is derived locally so the layout feels more alive without requiring
 * any additional repository or backend work.
 */
    val related = notification?.relatedEchoes().orEmpty()
    if (related.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
/**
 * Small row used to render each related echo.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        LayoutStateDot(LayoutAccent)
        Text(text = label, color = LayoutMuted, style = MaterialTheme.typography.bodySmall)
/**
 * A compact footer ledger for the whole screen.
 *
 * It closes the visual loop by summarizing the current selection and read state one more time.
 */
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutMetric("Unread", unreadCount.toString())
        LayoutMetric("Read", readCount.toString())
/**
 * Shared empty state for the feed and the detail panel.
 *
 * Centering the message keeps the composition from feeling broken when the data set is empty,
 * which is especially important for a disconnected design file like this one.
 */
    Box(
        modifier = Modifier
            .fillMaxWidth()
// Derived values are kept as small helper functions so the main composables stay easy to scan.
// These helpers also make it easier to swap the fake layout data for real state later.
private fun List<NotificationUiModel>.unreadCount(): Int = count { !it.isRead }
// These helpers keep the detail panel descriptive without introducing real data plumbing.
private fun NotificationUiModel.classificationLabel(): String =
    when {
        title.contains("payment", ignoreCase = true) -> "Financial"
        title.contains("trip", ignoreCase = true) -> "Trip"
        body.contains("offline", ignoreCase = true) -> "Offline"
        isRead -> "Archived"
        else -> "Actionable"
    }

private fun NotificationUiModel.relatedEchoes(): List<String> = listOf(
    "$timestampLabel - ${classificationLabel()}",
    if (isRead) "Already acknowledged locally" else "Still pending review",
    if (body.length > 50) "Expanded detail available" else "Short-form notice"
)
@file:Suppress("unused", "LongMethod", "MagicNumber")

package com.wheels.app.features.notifications.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// This file is intentionally standalone.
// It explores a different visual direction for notifications without wiring anything
// into navigation, persistence, ViewModels, or the rest of the app shell.

// The palette is local on purpose so the layout can feel editorial and distinct
// from the default app surfaces while still remaining readable and calm.
private val LayoutBackground = Color(0xFF07111F)
private val LayoutSurface = Color(0xFF0E1A2B)
private val LayoutSurfaceAlt = Color(0xFF14263D)
private val LayoutAccent = Color(0xFF70E1F5)
private val LayoutAccent2 = Color(0xFFFFC371)
private val LayoutMuted = Color.White.copy(alpha = 0.72f)
private val LayoutBorder = Color.White.copy(alpha = 0.08f)

// A radial gradient keeps the composition from feeling flat.
// The brighter center gives the two-column content a focal point without adding noise.
private val LayoutGradient = Brush.radialGradient(
    colors = listOf(LayoutSurface.copy(alpha = 0.98f), LayoutBackground),
    radius = 1800f
)

/**
 * Standalone notifications layout concept.
 *
 * Design intent:
 * - Left side: a dense triage feed for scanning many notifications quickly.
 * - Right side: a slower, more readable detail panel for the selected item.
 * - Top rail: a compact summary that communicates inbox state at a glance.
 *
 * This composable is intentionally disconnected from the app flow so it can be used
 * as a design reference, preview target, or later integration point without hidden side effects.
 */
@Composable
fun NotificationsLayout(
    notifications: List<NotificationUiModel> = emptyList(),
    selectedId: String? = null,
    modifier: Modifier = Modifier
) {
    // Resolve the selected notification once so both columns stay visually in sync.
    // If a selected ID is not found, the first available notification becomes the fallback.
    val selected = notifications.firstOrNull { it.id == selectedId } ?: notifications.firstOrNull()
    val unreadCount = notifications.unreadCount()
    val readCount = notifications.readCount()

    Surface(modifier = modifier.fillMaxSize(), color = LayoutBackground) {
        // The outer Box lets us paint the atmosphere behind the content without
        // complicating the feed and detail components with background concerns.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LayoutGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // The header provides immediate context: what this screen is for,
                // how many notifications exist, and what the inbox state feels like.
                NotificationsLayoutHeader(totalCount = notifications.size)
                NotificationsLayoutToolbar(
                    totalCount = notifications.size,
                    unreadCount = unreadCount,
                    readCount = readCount
                )
                NotificationsLayoutNarrativeStrip(
                    selected = selected,
                    totalCount = notifications.size,
                    unreadCount = unreadCount
                )

                // The two-column split is the main design choice.
                // The inbox gets slightly more width because list scanning is the dominant task.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NotificationsLayoutFeed(
                        notifications = notifications,
                        selectedId = selected?.id,
                        modifier = Modifier.fillMaxWidth(0.48f)
                    )
                    NotificationsLayoutDetail(
                        notification = selected,
                        modifier = Modifier.fillMaxWidth(0.48f)
                    )
                }

                NotificationsLayoutFooterLedger(
                    selected = selected,
                    unreadCount = unreadCount,
                    readCount = readCount
                )
            }
        }
    }
}

/**
 * Header block for the layout.
 *
 * The header uses a title, short explanatory copy, and a few metric chips so the
 * screen can communicate its current state before the user scrolls the feed.
 */
@Composable
private fun NotificationsLayoutHeader(totalCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Notification Atlas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "An alternative editorial layout for inbox scanning, detail reading, and future offline sync state.",
            color = LayoutMuted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LayoutMetric("Inbox", totalCount.toString())
            LayoutMetric("Offline", "Ready")
            LayoutMetric("Sync", "Queued")
        }
    }
}

/**
 * A light-weight toolbar that makes the layout feel more complete.
 *
 * The controls are intentionally cosmetic here. They describe the available inbox modes
 * without pretending the feature is already wired to state or navigation.
 */
@Composable
private fun NotificationsLayoutToolbar(
    totalCount: Int,
    unreadCount: Int,
    readCount: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        LayoutMetric("Total", totalCount.toString())
        LayoutMetric("Unread", unreadCount.toString())
        LayoutMetric("Read", readCount.toString())
        LayoutToolbarPill("All")
        LayoutToolbarPill("Unread")
        LayoutToolbarPill("Offline")
    }
}

/**
 * Small summary chip used by the header.
 *
 * The chip is intentionally understated: it should add orientation, not compete with the
 * actual notification content.
 */
@Composable
private fun LayoutMetric(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutSurfaceAlt.copy(alpha = 0.82f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text = label, color = LayoutMuted, style = MaterialTheme.typography.labelSmall)
            Text(text = value, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * A softer pill treatment used to sketch filter affordances.
 *
 * It reads more like a mode label than a button, which fits the current disconnected state.
 */
@Composable
private fun LayoutToolbarPill(label: String) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutBackground.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Text(
            text = label,
            color = LayoutMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * A wider narrative strip that explains the inbox state in plain language.
 *
 * This section helps the screen feel like a dashboard instead of just a two-pane list.
 */
@Composable
private fun NotificationsLayoutNarrativeStrip(
    selected: NotificationUiModel?,
    totalCount: Int,
    unreadCount: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutNarrativeCard(
            label = "Focus",
            value = selected?.title ?: "No selection",
            detail = "The detail pane follows the active notification."
        )
        LayoutNarrativeCard(
            label = "Volume",
            value = totalCount.toString(),
            detail = "A compact count that communicates inbox size."
        )
        LayoutNarrativeCard(
            label = "Attention",
            value = unreadCount.toString(),
            detail = "Unread items stay visually heavier until triaged."
        )
    }
}

/**
 * Narrative card used inside the strip above.
 *
 * The card mixes a label, a value, and a short explanation so the top region
 * can communicate context even before the user looks at the list.
 */
@Composable
private fun LayoutNarrativeCard(label: String, value: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(0.31f),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutSurfaceAlt.copy(alpha = 0.68f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = LayoutMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = LayoutMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Inbox column.
 *
 * The feed is designed to be dense enough for triage but still visually separated item by item,
 * which matters when a user is quickly reviewing many notifications after being away from the app.
 */
@Composable
private fun NotificationsLayoutFeed(
    notifications: List<NotificationUiModel>,
    selectedId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutSurface.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Text("Inbox", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(
                "A calm, dense feed designed for quick triage and offline browsing.",
                color = LayoutMuted
            )
            Spacer(Modifier.height(14.dp))
            NotificationsLayoutFilterRow(
                totalCount = notifications.size,
                unreadCount = notifications.unreadCount()
            )
            Spacer(Modifier.height(12.dp))

            // Empty-state handling lives inside the feed so the column still feels intentional
            // even when there is nothing to browse.
            Box(modifier = Modifier.weight(1f)) {
                if (notifications.isEmpty()) {
                    NotificationsLayoutEmptyState()
                } else {
                    // LazyColumn keeps the list efficient if the inbox grows large later.
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(notifications, key = { it.id }) { notification ->
                            NotificationsLayoutItem(
                                notification = notification,
                                selected = notification.id == selectedId
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            NotificationsLayoutFeedFooter(
                totalCount = notifications.size,
                unreadCount = notifications.unreadCount(),
                selectedId = selectedId
            )
        }
    }
}

/**
 * A compact row of filter chips that makes the feed feel more interactive.
 *
 * The chips are decorative for now, but the structure is ready for actual filter state later.
 */
@Composable
private fun NotificationsLayoutFilterRow(totalCount: Int, unreadCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LayoutFilterChip("All $totalCount", selected = true)
        LayoutFilterChip("Unread $unreadCount", selected = false)
        LayoutFilterChip("Offline", selected = false)
    }
}

/**
 * Filter chip used by the row above.
 *
 * The selected chip is brighter and a touch more grounded, so the user gets a quick
 * sense of the current mode without needing to parse secondary controls.
 */
@Composable
private fun LayoutFilterChip(label: String, selected: Boolean) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LayoutSurfaceAlt.copy(alpha = 0.9f) else LayoutBackground.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, if (selected) LayoutAccent.copy(alpha = 0.7f) else LayoutBorder)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else LayoutMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

/**
 * A single notification preview item.
 *
 * The visual treatment changes slightly when selected so the user always has a clear anchor
 * between the list and the detail pane.
 */
@Composable
private fun NotificationsLayoutItem(
    notification: NotificationUiModel,
    selected: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LayoutSurfaceAlt.copy(alpha = 0.95f) else LayoutBackground.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            1.dp,
            if (selected) LayoutAccent.copy(alpha = 0.75f) else LayoutBorder
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(notification.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(notification.body, color = LayoutMuted, style = MaterialTheme.typography.bodyMedium)
            NotificationsLayoutItemFooter(notification = notification, selected = selected)
        }
    }
}

/**
 * Footer row for each inbox item.
 *
 * It adds a little more structure around the timestamp and read state so the list can
 * communicate state at a glance without becoming visually busy.
 */
@Composable
private fun NotificationsLayoutItemFooter(
    notification: NotificationUiModel,
    selected: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${notification.timestampLabel} - ${if (notification.isRead) "read" else "unread"}",
            color = if (notification.isRead) LayoutMuted else LayoutAccent2,
            style = MaterialTheme.typography.labelMedium
        )
        LayoutStateDot(
            color = if (selected) LayoutAccent else if (notification.isRead) LayoutMuted else LayoutAccent2
        )
    }
}

/**
 * Tiny visual accent used in the item footer.
 *
 * A dot is enough here because it gives the layout a little rhythm without demanding attention.
 */
@Composable
private fun LayoutStateDot(color: Color) {
    Box(
        modifier = Modifier
            .width(8.dp)
            .height(8.dp)
            .background(color, RoundedCornerShape(99.dp))
    )
}

/**
 * Feed footer that frames the inbox as a working queue.
 *
 * It adds one more summary layer so the feed has a clear end point and a clearer role.
 */
@Composable
private fun NotificationsLayoutFeedFooter(
    totalCount: Int,
    unreadCount: Int,
    selectedId: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LayoutMetric("Visible", totalCount.toString())
            LayoutMetric("Pending", unreadCount.toString())
            LayoutMetric("Selected", selectedId ?: "None")
        }
        Text(
            text = "This feed is structured for triage, not just browsing.",
            color = LayoutMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Detail column.
 *
 * This panel is intentionally more spacious and more readable than the feed. That contrast
 * makes the layout feel like a workspace instead of a plain list.
 */
@Composable
private fun NotificationsLayoutDetail(
    notification: NotificationUiModel?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutSurfaceAlt.copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Detail", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(
                "This panel stays intentionally spacious so the user can read, compare, and decide without losing context.",
                color = LayoutMuted
            )
            NotificationsLayoutDetailSummary(notification = notification)
            NotificationsLayoutStateLegend(notification = notification)

            // The branch is explicit because the detail panel should communicate
            // whether the screen has a selected notification or is waiting for one.
            when (notification) {
                null -> NotificationsLayoutEmptyState()
                else -> {
                    Text(notification.title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text(notification.timestampLabel, color = LayoutAccent2)
                    Text(notification.body, color = LayoutMuted)
                    NotificationsLayoutActionRow()
                    NotificationsLayoutDetailNotes()
                }
            }
            NotificationsLayoutRelatedSection(notification = notification)
        }
    }
}

/**
 * Small detail summary cards that make the right column feel more editorial.
 *
 * The cards are read-only, but they hint at the kinds of metadata that would be surfaced
 * once the inbox is backed by a real repository and state source.
 */
@Composable
private fun NotificationsLayoutDetailSummary(notification: NotificationUiModel?) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutMetric("Selected", notification?.id ?: "None")
        LayoutMetric("State", if (notification?.isRead == true) "Read" else "Pending")
    }
}

/**
 * Action row that sketches future inbox operations.
 *
 * These are decorative placeholders for now, but they help tell the story of the feature:
 * notifications are not just viewed, they are managed.
 */
@Composable
private fun NotificationsLayoutActionRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutActionBadge("Mark read")
        LayoutActionBadge("Delete")
        LayoutActionBadge("Sync later")
    }
}

/**
 * Reusable action badge for the detail panel.
 *
 * The badge has the feel of a command chip rather than a destructive button because the
 * file is still a static concept rather than a live interaction layer.
 */
@Composable
private fun LayoutActionBadge(label: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LayoutBackground.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, LayoutBorder)
    ) {
        Text(
            text = label,
            color = LayoutMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * Supporting copy for the detail pane.
 *
 * The extra notes preserve the current story of the screen: local-first browsing, eventual
 * sync, and a clear separation between what is visible now and what will be wired later.
 */
@Composable
private fun NotificationsLayoutDetailNotes() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "The layout leaves space for future read-state, offline state, and queued actions without wiring them yet.",
            color = LayoutMuted
        )
        Text(
            "The right pane is intentionally quieter so that notification text stays readable even when the feed is crowded.",
            color = LayoutMuted
        )
    }
}

/**
 * A small legend that explains the current state of the detail pane.
 *
 * These tags are deliberately lightweight so they work like labels, not buttons.
 */
@Composable
private fun NotificationsLayoutStateLegend(notification: NotificationUiModel?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LayoutToolbarPill(if (notification == null) "Waiting" else "Loaded")
        LayoutToolbarPill(if (notification?.isRead == true) "Read" else "Unread")
        LayoutToolbarPill(notification?.classificationLabel() ?: "Empty")
    }
}

/**
 * A related-items section that gives the detail pane more depth.
 *
 * The content is derived locally so the layout feels more alive without requiring
 * any additional repository or backend work.
 */
@Composable
private fun NotificationsLayoutRelatedSection(notification: NotificationUiModel?) {
    val related = notification?.relatedEchoes().orEmpty()
    if (related.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Related echoes", color = Color.White, style = MaterialTheme.typography.titleSmall)
            related.forEach { echo ->
                LayoutRelatedEcho(echo)
            }
        }
    }
}

/**
 * Small row used to render each related echo.
 */
@Composable
private fun LayoutRelatedEcho(label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        LayoutStateDot(LayoutAccent)
        Text(text = label, color = LayoutMuted, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * A compact footer ledger for the whole screen.
 *
 * It closes the visual loop by summarizing the current selection and read state one more time.
 */
@Composable
private fun NotificationsLayoutFooterLedger(
    selected: NotificationUiModel?,
    unreadCount: Int,
    readCount: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LayoutMetric("Unread", unreadCount.toString())
        LayoutMetric("Read", readCount.toString())
        LayoutMetric("Mode", selected?.classificationLabel() ?: "None")
    }
    if (selected != null) {
        Text(
            text = "Selected: ${selected.title} - ${selected.classificationLabel()}",
            color = LayoutMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Shared empty state for the feed and the detail panel.
 *
 * Centering the message keeps the composition from feeling broken when the data set is empty,
 * which is especially important for a disconnected design file like this one.
 */
@Composable
private fun NotificationsLayoutEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No notifications staged for this preview.", color = LayoutMuted)
    }
}

// Derived values are kept as small helper functions so the main composables stay easy to scan.
// These helpers also make it easier to swap the fake layout data for real state later.
private fun List<NotificationUiModel>.unreadCount(): Int = count { !it.isRead }
private fun List<NotificationUiModel>.readCount(): Int = count { it.isRead }

// These helpers keep the detail panel descriptive without introducing real data plumbing.
private fun NotificationUiModel.classificationLabel(): String =
    when {
        title.contains("payment", ignoreCase = true) -> "Financial"
        title.contains("trip", ignoreCase = true) -> "Trip"
        body.contains("offline", ignoreCase = true) -> "Offline"
        isRead -> "Archived"
        else -> "Actionable"
    }

private fun NotificationUiModel.relatedEchoes(): List<String> = listOf(
    "$timestampLabel - ${classificationLabel()}",
    if (isRead) "Already acknowledged locally" else "Still pending review",
    if (body.length > 50) "Expanded detail available" else "Short-form notice"
)
