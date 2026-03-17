package com.wheels.app.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wheels.app.core.navigation.BottomNavItem
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsSurface

@Composable
fun WheelsBottomBar(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WheelsSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            item.usesAvatar -> ProfileBubble(selected = selected)
            selected -> SelectedIconBubble(item = item)
            else -> InactiveIconBubble(item = item)
        }

        Text(
            text = item.label,
            color = if (selected) PrimaryBlue else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SelectedIconBubble(item: BottomNavItem) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = PrimaryBlue
    ) {
        Box(contentAlignment = Alignment.Center) {
            BadgedIcon(item = item, selected = true)
        }
    }
}

@Composable
private fun InactiveIconBubble(item: BottomNavItem) {
    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        BadgedIcon(item = item, selected = false)
    }
}

@Composable
private fun ProfileBubble(selected: Boolean) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = if (selected) PrimaryBlue else androidx.compose.ui.graphics.Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Profile icon would go here
        }
    }
}

@Composable
private fun BadgedIcon(item: BottomNavItem, selected: Boolean) {
    // Icon implementation would go here
}
