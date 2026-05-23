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
import androidx.compose.foundation.layout.weight
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
// It provides a second notifications layout option with a brighter, gallery-like
// composition instead of the darker split-pane treatment from NotificationsLayout.kt.

private val GalleryBackground = Color(0xFFF4F0E7)
private val GallerySurface = Color(0xFFFFFCF8)
private val GallerySurfaceAlt = Color(0xFFF0E7D8)
private val GallerySurfaceDeep = Color(0xFFDAD1BF)
private val GalleryInk = Color(0xFF1B2430)
private val GalleryMuted = Color(0xFF5E6975)
private val GalleryAccent = Color(0xFF2E7D6B)
private val GalleryAccentWarm = Color(0xFFC96F4A)
private val GalleryAccentSoft = Color(0xFF8A9865)
private val GalleryBorder = Color(0xFFD4DAE0)

// The backdrop uses two layers of light and a pair of soft glows.
// That keeps the page from feeling flat while the cards do the real work.
private val GalleryBackdrop = Brush.linearGradient(
    colors = listOf(GalleryBackground, GallerySurface.copy(alpha = 0.96f), GallerySurfaceAlt.copy(alpha = 0.92f))
)
private val GalleryGlow = Brush.radialGradient(
    colors = listOf(GalleryAccent.copy(alpha = 0.13f), Color.Transparent),
    radius = 1400f
)
private val GalleryWarmGlow = Brush.radialGradient(
    colors = listOf(GalleryAccentWarm.copy(alpha = 0.10f), Color.Transparent),
    radius = 1100f
)

/**
 * Alternate notifications layout.
 *
 * This version is brighter and more vertical than the original split layout.
 * It starts with a spotlight and then a compact inbox board.
 */
@Composable
fun NotificationsLayoutGallery(
    notifications: List<NotificationUiModel> = emptyList(),
    selectedId: String? = null,
    modifier: Modifier = Modifier
) {
    // Resolve the active item once so every panel stays in sync.
    val selected = notifications.firstOrNull { it.id == selectedId } ?: notifications.firstOrNull()
    val unreadCount = notifications.galleryUnreadCount()
    val readCount = notifications.galleryReadCount()
    val priorityCount = notifications.galleryPriorityCount()

    Surface(modifier = modifier.fillMaxSize(), color = GalleryBackground) {
        Box(modifier = Modifier.fillMaxSize().background(GalleryBackdrop)) {
            NotificationsLayoutGalleryAtmosphere()
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                NotificationsLayoutGalleryHeader(
                    totalCount = notifications.size,
                    unreadCount = unreadCount,
                    readCount = readCount,
                    priorityCount = priorityCount,
                    selected = selected
                )
                NotificationsLayoutGallerySpotlight(notification = selected)
                Box(modifier = Modifier.weight(1f)) {
                    NotificationsLayoutGalleryContentGrid(
                        notifications = notifications.galleryOrdered(),
                        selectedId = selected?.id,
                        unreadCount = unreadCount,
                        priorityCount = priorityCount
                    )
                }
            }
        }
    }
}

/**
 * Soft background decoration.
 *
 * The glows are ambient only. They add depth without competing with the content.
 */
@Composable
private fun NotificationsLayoutGalleryAtmosphere() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 12.dp)
                .width(260.dp)
                .height(260.dp)
                .background(GalleryGlow, RoundedCornerShape(999.dp))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 24.dp)
                .width(220.dp)
                .height(220.dp)
                .background(GalleryWarmGlow, RoundedCornerShape(999.dp))
        )
    }
}

/**
 * Header block with title, short explanation, and a compact metric rail.
 */
@Composable
private fun NotificationsLayoutGalleryHeader(
    totalCount: Int,
    unreadCount: Int,
    readCount: Int,
    priorityCount: Int,
    selected: NotificationUiModel?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Notification Gallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = GalleryInk
        )
        Text(
            text = "A brighter, paper-board alternative that moves the active notification into a spotlight and keeps triage visible beside it.",
            color = GalleryMuted
        )
        NotificationsLayoutGalleryHeaderMetrics(
            totalCount = totalCount,
            unreadCount = unreadCount,
            readCount = readCount,
            priorityCount = priorityCount,
            selected = selected
        )
    }
}

/**
 * A small horizontal strip of summary chips.
 */
@Composable
private fun NotificationsLayoutGalleryHeaderMetrics(
    totalCount: Int,
    unreadCount: Int,
    readCount: Int,
    priorityCount: Int,
    selected: NotificationUiModel?
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NotificationsLayoutGalleryMetricChip("Total", totalCount.toString())
        NotificationsLayoutGalleryMetricChip("Unread", unreadCount.toString())
        NotificationsLayoutGalleryMetricChip("Read", readCount.toString())
        NotificationsLayoutGalleryMetricChip("Priority", priorityCount.toString())
        NotificationsLayoutGalleryMetricChip("Selected", selected?.id ?: "None")
    }
}

/**
 * Shared metric chip used by the header rail.
 */
@Composable
private fun NotificationsLayoutGalleryMetricChip(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GallerySurface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, GalleryBorder)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text = label, color = GalleryMuted, style = MaterialTheme.typography.labelSmall)
            Text(text = value, color = GalleryInk, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun NotificationsLayoutGalleryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GalleryBorder.copy(alpha = 0.78f))
    )
}

@Composable
private fun NotificationsLayoutGallerySpotlight(notification: NotificationUiModel?) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = GallerySurface.copy(alpha = 0.90f)),
        border = BorderStroke(1.dp, GalleryBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NotificationsLayoutGallerySectionHeader(
                title = "Spotlight",
                detail = "The selected notification gets the clearest reading position on the screen."
            )
            NotificationsLayoutGalleryDivider()
            if (notification == null) {
                NotificationsLayoutGalleryEmptyState(
                    title = "No notification selected",
                    detail = "Pick an item from the board below so the spotlight can expand it."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(10.dp)
                                .background(
                                    when {
                                        notification.isRead -> GalleryAccentSoft
                                        notification.isGalleryPriority() -> GalleryAccentWarm
                                        else -> GalleryAccent
                                    },
                                    RoundedCornerShape(99.dp)
                                )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = notification.title,
                                color = GalleryInk,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = notification.timestampLabel,
                                color = GalleryAccentWarm,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationsLayoutGalleryTag(
                            label = notification.galleryStatusLabel(),
                            accent = if (notification.isRead) GalleryAccentSoft else GalleryAccentWarm
                        )
                        NotificationsLayoutGalleryTag(
                            label = notification.galleryThemeLabel(),
                            accent = GalleryAccent
                        )
                        NotificationsLayoutGalleryTag(
                            label = if (notification.isGalleryPriority()) "Priority" else "Standard",
                            accent = if (notification.isGalleryPriority()) GalleryAccentWarm else GalleryAccentSoft
                        )
                    }
                    Text(
                        text = notification.body,
                        color = GalleryMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (notification.isGalleryPriority()) {
                            "Priority lane keeps this item near the top."
                        } else {
                            "Regular lane keeps it calm and readable."
                        },
                        color = GalleryMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsLayoutGallerySectionHeader(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = GalleryInk,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = detail, color = GalleryMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun NotificationsLayoutGalleryTag(label: String, accent: Color) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = GallerySurfaceAlt.copy(alpha = 0.66f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f))
    ) {
        Text(
            text = label,
            color = GalleryInk,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun NotificationsLayoutGalleryContentGrid(
    notifications: List<NotificationUiModel>,
    selectedId: String?,
    unreadCount: Int,
    priorityCount: Int
) {
    NotificationsLayoutGalleryInboxPanel(
        notifications = notifications,
        selectedId = selectedId,
        unreadCount = unreadCount,
        priorityCount = priorityCount
    )
}

@Composable
private fun NotificationsLayoutGalleryInboxPanel(
    notifications: List<NotificationUiModel>,
    selectedId: String?,
    unreadCount: Int,
    priorityCount: Int
) {
    Card(
        modifier = Modifier.fillMaxHeight(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = GallerySurface.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, GalleryBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NotificationsLayoutGallerySectionHeader(
                title = "Inbox board",
                detail = "Items here are arranged for quick triage, not just passive browsing."
            )
            NotificationsLayoutGalleryInboxFilters(
                totalCount = notifications.size,
                unreadCount = unreadCount,
                priorityCount = priorityCount
            )
            NotificationsLayoutGalleryDivider()
            Box(modifier = Modifier.weight(1f)) {
                if (notifications.isEmpty()) {
                    NotificationsLayoutGalleryEmptyState(
                        title = "No notifications staged yet",
                        detail = "The inbox board is ready when the first item arrives."
                    )
                } else {
                    NotificationsLayoutGalleryNotificationList(
                        notifications = notifications,
                        selectedId = selectedId
                    )
                }
            }
            Text(
                text = notifications.galleryBoardSentence(),
                color = GalleryMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NotificationsLayoutGalleryInboxFilters(
    totalCount: Int,
    unreadCount: Int,
    priorityCount: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NotificationsLayoutGalleryFilterChip("All $totalCount", selected = true)
        NotificationsLayoutGalleryFilterChip("Unread $unreadCount", selected = false)
        NotificationsLayoutGalleryFilterChip("Priority $priorityCount", selected = false)
    }
}

@Composable
private fun NotificationsLayoutGalleryFilterChip(label: String, selected: Boolean) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) GallerySurfaceDeep.copy(alpha = 0.88f) else GallerySurface.copy(alpha = 0.68f)
        ),
        border = BorderStroke(1.dp, if (selected) GalleryAccent.copy(alpha = 0.75f) else GalleryBorder)
    ) {
        Text(
            text = label,
            color = if (selected) GalleryInk else GalleryMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun NotificationsLayoutGalleryNotificationList(
    notifications: List<NotificationUiModel>,
    selectedId: String?
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 2.dp)
    ) {
        items(notifications, key = { it.id }) { notification ->
            NotificationsLayoutGalleryNotificationCard(
                notification = notification,
                selected = notification.id == selectedId
            )
        }
    }
}

@Composable
private fun NotificationsLayoutGalleryNotificationCard(
    notification: NotificationUiModel,
    selected: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> GallerySurfaceAlt.copy(alpha = 0.94f)
                notification.isRead -> GallerySurface.copy(alpha = 0.72f)
                else -> GallerySurface.copy(alpha = 0.96f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                selected -> GalleryAccent.copy(alpha = 0.78f)
                notification.isGalleryPriority() -> GalleryAccentWarm.copy(alpha = 0.78f)
                else -> GalleryBorder
            }
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NotificationsLayoutGalleryNotificationHeader(
                notification = notification,
                selected = selected
            )
            Text(text = notification.body, color = GalleryMuted, style = MaterialTheme.typography.bodyMedium)
            NotificationsLayoutGalleryNotificationTags(notification = notification)
            NotificationsLayoutGalleryNotificationMeta(notification = notification)
        }
    }
}

@Composable
private fun NotificationsLayoutGalleryNotificationHeader(
    notification: NotificationUiModel,
    selected: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(9.dp)
                .height(9.dp)
                .background(
                    when {
                        selected -> GalleryAccent
                        notification.isGalleryPriority() -> GalleryAccentWarm
                        else -> GalleryAccentSoft
                    },
                    RoundedCornerShape(99.dp)
                )
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = notification.title,
                color = GalleryInk,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(text = notification.timestampLabel, color = GalleryMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun NotificationsLayoutGalleryNotificationTags(notification: NotificationUiModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NotificationsLayoutGalleryTag(notification.galleryThemeLabel(), GalleryAccent)
        NotificationsLayoutGalleryTag(
            notification.galleryStatusLabel(),
            if (notification.isRead) GalleryAccentSoft else GalleryAccentWarm
        )
        NotificationsLayoutGalleryTag(
            if (notification.isGalleryPriority()) "Priority" else "Standard",
            if (notification.isGalleryPriority()) GalleryAccentWarm else GalleryAccentSoft
        )
    }
}

@Composable
private fun NotificationsLayoutGalleryNotificationMeta(notification: NotificationUiModel) {
    Text(
        text = "${notification.timestampLabel} · ${if (notification.isRead) "read" else "unread"}",
        color = if (notification.isRead) GalleryMuted else GalleryAccentWarm,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun NotificationsLayoutGalleryEmptyState(
    title: String = "No notifications staged",
    detail: String = "The layout is ready whenever new items arrive."
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = GalleryInk, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = GalleryMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun NotificationUiModel.isGalleryPriority(): Boolean {
    val titleMatch = listOf("alert", "payment", "urgent", "security", "trip", "invoice", "delivery")
        .any { token -> title.contains(token, ignoreCase = true) }
    val bodyMatch = listOf("urgent", "deadline", "action", "offline", "fail", "error")
        .any { token -> body.contains(token, ignoreCase = true) }
    return !isRead && (titleMatch || bodyMatch)
}

private fun NotificationUiModel.galleryThemeLabel(): String =
    when {
        title.contains("payment", ignoreCase = true) -> "Finance"
        title.contains("trip", ignoreCase = true) -> "Travel"
        title.contains("invoice", ignoreCase = true) -> "Billing"
        title.contains("delivery", ignoreCase = true) -> "Delivery"
        title.contains("security", ignoreCase = true) -> "Security"
        body.contains("offline", ignoreCase = true) -> "Offline"
        isRead -> "Archived"
        else -> "General"
    }

private fun NotificationUiModel.galleryStatusLabel(): String = if (isRead) "Read" else "Unread"

private fun List<NotificationUiModel>.galleryBoardSentence(): String =
    when {
        isEmpty() -> "The board is empty and ready for the first notification."
        galleryPriorityCount() > 0 -> "${galleryPriorityCount()} priority item(s) sit ahead of the rest."
        galleryUnreadCount() > 0 -> "${galleryUnreadCount()} unread item(s) remain visible for triage."
        else -> "Everything is read, so the board feels calm and settled."
    }

private fun List<NotificationUiModel>.galleryOrdered(): List<NotificationUiModel> =
    sortedWith(
        compareByDescending<NotificationUiModel> { it.isGalleryPriority() }
            .thenBy { it.isRead }
            .thenBy { it.title }
    )

private fun List<NotificationUiModel>.galleryUnreadCount(): Int = count { !it.isRead }
private fun List<NotificationUiModel>.galleryReadCount(): Int = count { it.isRead }
private fun List<NotificationUiModel>.galleryPriorityCount(): Int = count { it.isGalleryPriority() }
