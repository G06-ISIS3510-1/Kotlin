package com.wheels.app.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheels.app.core.navigation.BottomNavItem

@Composable
fun WheelsBottomBar(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.outline,
                thickness = 1.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomBarItem(
                        item = item,
                        selected = item.route == selectedRoute,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            item.usesAvatar -> ProfileBubble(selected = selected)
            selected -> SelectedIconBubble(item = item)
            else -> InactiveIconBubble(item = item)
        }

        Text(
            text = item.label,
            color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SelectedIconBubble(item: BottomNavItem) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = colorScheme.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            BadgedIcon(item = item, selected = true)
        }
    }
}

@Composable
private fun InactiveIconBubble(item: BottomNavItem) {
    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        BadgedIcon(item = item, selected = false)
    }
}

@Composable
private fun ProfileBubble(selected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = if (selected) colorScheme.primary else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = "Profile",
                tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BadgedIcon(item: BottomNavItem, selected: Boolean) {
    val iconTint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    if ((item.badgeCount ?: 0) > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = item.badgeCount.toString())
                }
            }
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconTint
            )
        }
    } else {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint
        )
    }
}
