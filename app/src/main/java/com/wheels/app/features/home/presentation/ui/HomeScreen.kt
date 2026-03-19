package com.wheels.app.features.home.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.home.presentation.viewmodel.ActiveRideUiModel
import com.wheels.app.features.home.presentation.viewmodel.HomeQuickStat
import com.wheels.app.features.home.presentation.viewmodel.HomeUpdateUiModel
import com.wheels.app.features.home.presentation.viewmodel.HomeUiState
import com.wheels.app.features.home.presentation.viewmodel.HomeViewModel
import com.wheels.app.features.home.presentation.viewmodel.UpdateTone

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    viewModel: HomeViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WheelsBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            item { HeaderSection(state) }
            item { MapSection(state.activeRide) }
            item {
                CurrentRideSection(
                    activeRide = state.activeRide,
                    onOpenChat = { navController.navigate(Destinations.GroupChat.route) },
                    onOpenReviews = {
                        navController.navigate(
                            Destinations.ReviewsRatings.createRoute(state.activeRide.driver)
                        )
                    }
                )
            }
            item {
                Text(
                    text = "Updates",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
            }
            items(state.updates) { update ->
                UpdateCard(
                    update = update,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        QuickPayButton(
            navController = navController,
            modifier = Modifier
                .padding(end = 20.dp, bottom = 140.dp)
                .shadow(20.dp, RoundedCornerShape(999.dp), spotColor = ElectricGreen.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun HeaderSection(state: HomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = WheelsSurface.copy(alpha = 0.10f)) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = WheelsSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = state.welcomeMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = WheelsSurface.copy(alpha = 0.72f)
                    )
                    Text(
                        text = state.userName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WheelsSurface
                    )
                }
            }

            Box(contentAlignment = Alignment.TopEnd) {
                Surface(shape = CircleShape, color = WheelsSurface.copy(alpha = 0.10f)) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = WheelsSurface
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-6).dp, y = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ElectricGreen)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.quickStats.forEach { stat ->
                StatChip(stat = stat, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(stat: HomeQuickStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WheelsSurface.copy(alpha = 0.10f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stat.value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = stat.accentColor ?: WheelsSurface
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodySmall,
            color = WheelsSurface.copy(alpha = 0.70f)
        )
    }
}

@Composable
private fun MapSection(activeRide: ActiveRideUiModel) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .offset(y = (-12).dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFE8F0F9), WheelsBackground)))
        ) {
            MapGridBackground()
            RoutePath()
            EtaBadge(
                etaMinutes = activeRide.etaMinutes,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
            DriverMarker(modifier = Modifier.align(Alignment.Center))

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 72.dp, bottom = 56.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(SecondaryBlue)
                    .border(2.dp, WheelsSurface, CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 54.dp, end = 76.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .border(2.dp, WheelsSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Destination",
                    tint = WheelsSurface,
                    modifier = Modifier.size(12.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                color = WheelsSurface,
                shadowElevation = 8.dp
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Center map",
                        tint = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun MapGridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridColor = SecondaryBlue.copy(alpha = 0.18f)
        val verticalStops = listOf(size.width * 0.25f, size.width * 0.5f, size.width * 0.75f)
        val horizontalStops = listOf(size.height * 0.25f, size.height * 0.5f, size.height * 0.75f)
        verticalStops.forEach { x ->
            drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        }
        horizontalStops.forEach { y ->
            drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        }
    }
}

@Composable
private fun RoutePath() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = SecondaryBlue.copy(alpha = 0.60f),
            start = Offset(size.width * 0.20f, size.height * 0.78f),
            end = Offset(size.width * 0.82f, size.height * 0.22f),
            strokeWidth = 8f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
        )
    }
}

@Composable
private fun EtaBadge(etaMinutes: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = WheelsSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = "ETA",
                tint = ElectricGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$etaMinutes min away",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun DriverMarker(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ElectricGreen.copy(alpha = 0.18f))
        )
        Surface(
            modifier = Modifier.size(62.dp),
            shape = CircleShape,
            color = WheelsSurface,
            border = BorderStroke(4.dp, ElectricGreen),
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = "Driver",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun CurrentRideSection(
    activeRide: ActiveRideUiModel,
    onOpenChat: () -> Unit,
    onOpenReviews: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Ride",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ElectricGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Active",
                        color = ElectricGreen,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue)))
                        .clickable(onClick = onOpenReviews),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CM",
                        color = WheelsSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeRide.driver,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue,
                        modifier = Modifier.clickable(onClick = onOpenReviews)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenReviews)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA726),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = activeRide.rating, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(text = "  •  ${activeRide.carModel}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Text(text = activeRide.licensePlate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircleActionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        onClick = onOpenChat
                    )
                    CircleActionButton(icon = Icons.Default.Phone, contentDescription = "Call")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Border)
            Spacer(modifier = Modifier.height(16.dp))

            RoutePoint(
                title = "Pickup",
                location = activeRide.pickupLocation,
                leading = {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SecondaryBlue)
                    )
                }
            )

            Box(
                modifier = Modifier
                    .padding(start = 3.dp, top = 4.dp, bottom = 4.dp)
                    .height(18.dp)
                    .border(1.dp, Border, RoundedCornerShape(2.dp))
                    .width(0.dp)
            )

            RoutePoint(
                title = "Destination",
                location = activeRide.destination,
                leading = {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Destination",
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Trip Progress", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = "${activeRide.routeProgress}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(progress = activeRide.routeProgress / 100f)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailPill(title = "Distance", value = activeRide.distance, modifier = Modifier.weight(1f))
                DetailPill(title = "Fare", value = activeRide.fare, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    Surface(shape = CircleShape, color = Color(0xFFE8F0F9)) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = Color(0xFF5B89C8))
        }
    }
}

@Composable
private fun RoutePoint(title: String, location: String, leading: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.width(18.dp), contentAlignment = Alignment.TopCenter) { leading() }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFE8F0F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF5B89C8), ElectricGreen)))
        )
    }
}

@Composable
private fun DetailPill(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WheelsBackground)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
    }
}

@Composable
private fun UpdateCard(update: HomeUpdateUiModel, modifier: Modifier = Modifier) {
    val containerColor = when (update.tone) {
        UpdateTone.Success -> ElectricGreen.copy(alpha = 0.10f)
        UpdateTone.Info -> WheelsSurface
    }
    val borderColor = when (update.tone) {
        UpdateTone.Success -> ElectricGreen.copy(alpha = 0.20f)
        UpdateTone.Info -> Border
    }
    val iconBg = when (update.tone) {
        UpdateTone.Success -> ElectricGreen
        UpdateTone.Info -> SecondaryBlue.copy(alpha = 0.10f)
    }
    val iconTint = when (update.tone) {
        UpdateTone.Success -> WheelsSurface
        UpdateTone.Info -> SecondaryBlue
    }
    val icon = when (update.tone) {
        UpdateTone.Success -> Icons.Outlined.Navigation
        UpdateTone.Info -> Icons.Default.Star
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = update.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = PrimaryBlue
                )
                Text(text = update.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = update.timestamp, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun QuickPayButton(navController: NavController, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            navController.navigate(Destinations.QuickPayment.route)
        },
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = ElectricGreen,
            contentColor = WheelsSurface
        )
    ) {
        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Quick Pay", style = MaterialTheme.typography.labelLarge)
    }
}
