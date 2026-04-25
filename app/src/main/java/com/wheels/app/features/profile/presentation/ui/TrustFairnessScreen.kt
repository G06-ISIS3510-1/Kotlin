package com.wheels.app.features.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.ui.theme.GradientHeaderPrimaryContent
import com.wheels.app.core.ui.theme.GradientHeaderSecondaryContent
import com.wheels.app.features.profile.presentation.viewmodel.ProfileViewModel

@Composable
fun TrustFairnessScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val colors = if (colorScheme.background.luminance() < 0.5f) {
        TrustFairnessPalette(
            background = colorScheme.background,
            surface = colorScheme.surface,
            primary = Color(0xFFBFDBFE),
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            danger = Color(0xFFF87171)
        )
    } else {
        TrustFairnessPalette(
            background = colorScheme.background,
            surface = colorScheme.surface,
            primary = Color(0xFF1A3A5C),
            textPrimary = colorScheme.onSurface,
            textSecondary = colorScheme.onSurfaceVariant,
            success = Color(0xFF00D9A3),
            warning = Color(0xFFFFA726),
            danger = Color(0xFFEF4444)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A3A5C), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .padding(start = 12.dp, end = 20.dp, top = 28.dp, bottom = 24.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GradientHeaderPrimaryContent
                        )
                    }
                    Text(
                        text = "Trust & Fairness",
                        color = GradientHeaderPrimaryContent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Current contract for reliability score, cancellations and community accountability.",
                        color = GradientHeaderSecondaryContent,
                        fontSize = 12.sp
                    )
                }
            }

            if (state.activeRole != UserRole.DRIVER) {
                item {
                    MessageCard(
                        title = "Driver trust is not active in passenger mode",
                        body = "Passenger and driver reputations are treated separately. For now, the reliability score only applies when you are using Wheels as a driver.",
                        icon = Icons.Outlined.Info,
                        accent = colors.primary,
                        colors = colors,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                item {
                    SummaryCard(
                        trustScore = state.trustScore,
                        isLoading = state.trustScoreLoading,
                        colors = colors,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                MessageCard(
                    title = "How the score works",
                    body = "Formula: clamp(100 + completed ride bonus - cancellation penalty, 0, 100). Completed rides add +1 each, up to a maximum bonus of +10.",
                    icon = Icons.Outlined.Shield,
                    accent = colors.success,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                PolicyCard(
                    title = "Driver cancellation penalties",
                    rows = listOf(
                        "> 12 hours before departure" to "-2 points",
                        "3 to 12 hours before departure" to "-5 points",
                        "1 to 3 hours before departure" to "-10 points",
                        "Less than 1 hour before departure" to "-15 points"
                    ),
                    icon = Icons.Outlined.Warning,
                    accent = colors.danger,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                MessageCard(
                    title = "Behavioral nudge trigger",
                    body = "Wheels shows a preventive nudge in Create Ride when a driver has at least 2 cancellations and an average cancellation time of 24 hours or less before departure.",
                    icon = Icons.Outlined.Schedule,
                    accent = colors.warning,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                MessageCard(
                    title = "Analytics and accountability",
                    body = "Every driver cancellation creates an analytics event with userId, rideId, role, cancelledAt, cancellation hour, day of week, hours before departure and cancellation type. This supports our Q6 dashboard about how frequently a user cancels rides.",
                    icon = Icons.Outlined.Info,
                    accent = colors.primary,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Text(
                    text = "Passenger-side fairness rules and passenger trust score are still pending definition in this version.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    trustScore: Int?,
    isLoading: Boolean,
    colors: TrustFairnessPalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Driver reliability score",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    isLoading -> "Loading..."
                    trustScore != null -> "$trustScore / 100"
                    else -> "Not available yet"
                },
                color = colors.success,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "This score is recalculated in Firebase when you complete or cancel rides as a driver.",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    colors: TrustFairnessPalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PolicyCard(
    title: String,
    rows: List<Pair<String, String>>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    colors: TrustFairnessPalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = colors.textSecondary, fontSize = 12.sp)
                    Text(text = value, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class TrustFairnessPalette(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color,
    val warning: Color,
    val danger: Color
)
