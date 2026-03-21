package com.wheels.app.features.profile.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wheels.app.features.profile.presentation.viewmodel.ProfileViewModel
import kotlin.math.roundToInt

@Composable
fun TrustFairnessScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val colors = trustFairnessColors(forceDark = state.trustFairnessDarkMode)

    val reliabilityScore = state.trustScore ?: 98
    val totalRides = state.ridesCount.coerceAtLeast(1)
    val onTimePayments = (totalRides * (reliabilityScore / 100f)).roundToInt().coerceIn(0, totalRides)
    val cancellations = (totalRides - onTimePayments).coerceAtLeast(0)
    val punctualityRate = (reliabilityScore - 4).coerceIn(0, 100)
    val rewardPoints = 142

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colors.headerBackground,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(start = 12.dp, end = 24.dp, top = 28.dp, bottom = 22.dp)
                ) {
                    Column {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.onHeader
                            )
                        }

                        Text(
                            text = "Trust & Fairness",
                            color = colors.onHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Your reliability and accountability metrics",
                            color = colors.onHeader.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = colors.shadowColor
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ReliabilityGauge(score = reliabilityScore, colors = colors)

                        Text(
                            text = if (reliabilityScore >= 90) "Excellent Reliability!" else "Keep Improving",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            text = if (reliabilityScore >= 90) {
                                "You're in the top 10% of users"
                            } else {
                                "Stay consistent to improve your ranking"
                            },
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickMetricChip(
                                modifier = Modifier.weight(1f),
                                iconTint = colors.success,
                                value = onTimePayments.toString(),
                                label = "On-time pays",
                                colors = colors
                            )
                            QuickMetricChip(
                                modifier = Modifier.weight(1f),
                                iconTint = colors.info,
                                value = "$punctualityRate%",
                                label = "Punctual",
                                colors = colors
                            )
                            QuickMetricChip(
                                modifier = Modifier.weight(1f),
                                iconTint = colors.warning,
                                value = cancellations.toString(),
                                label = "Cancelled",
                                colors = colors
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Performance Breakdown",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    colors = colors
                )
            }

            item {
                MetricCard(
                    title = "Payment Reliability",
                    subtitle = "Track record of timely payments",
                    tint = colors.success,
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = "Payment",
                            tint = colors.success,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    content = {
                        val rate = ((onTimePayments / totalRides.toFloat()) * 100).roundToInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("On-time payments", color = colors.textSecondary, fontSize = 12.sp)
                            Text(
                                "$onTimePayments/$totalRides",
                                color = colors.success,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(colors.surfaceAlt)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(rate / 100f)
                                    .height(8.dp)
                                    .background(colors.success, RoundedCornerShape(99.dp))
                            )
                        }

                        Text(
                            text = "$rate% payment success rate",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                )
            }

            item {
                MetricCard(
                    title = "Punctuality Score",
                    subtitle = "Arrival time and waiting detection",
                    tint = colors.info,
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Punctuality",
                            tint = colors.info,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Average arrival time", color = colors.textSecondary, fontSize = 12.sp)
                            Text("2 min early", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Avg driver wait time", color = colors.textSecondary, fontSize = 12.sp)
                            Text("1.5 min", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.success.copy(alpha = 0.12f))
                                .border(1.dp, colors.success.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.TrendingUp,
                                    contentDescription = "Trend",
                                    tint = colors.success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Great punctuality! Keep it up",
                                    color = colors.success,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                )
            }

            item {
                MetricCard(
                    title = "Cancellation Record",
                    subtitle = "Last-minute cancellations impact score",
                    tint = colors.warning,
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Cancellation",
                            tint = colors.warning,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    content = {
                        val cancellationRate = ((cancellations / totalRides.toFloat()) * 100).roundToInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total cancellations", color = colors.textSecondary, fontSize = 12.sp)
                            Text(cancellations.toString(), color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cancellation rate", color = colors.textSecondary, fontSize = 12.sp)
                            Text("$cancellationRate%", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        if (cancellations > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.warning.copy(alpha = 0.12f))
                                    .border(1.dp, colors.warning.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Note: Multiple cancellations may affect your reliability score and access to rides.",
                                    color = colors.textPrimary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                )
            }

            item {
                SectionTitle(
                    title = "Accountability System",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    colors = colors
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = colors.shadowColor
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = "Policy",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Cancellation Policy",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        PolicyItem(
                            index = "1",
                            dotColor = colors.success,
                            title = "Free cancellation",
                            description = "Cancel up to 30 minutes before departure without penalty",
                            modifier = Modifier.padding(top = 14.dp),
                            colors = colors
                        )
                        PolicyItem(
                            index = "2",
                            dotColor = colors.warning,
                            title = "Late cancellation",
                            description = "Cancelling within 30 min results in a -10 point penalty",
                            modifier = Modifier.padding(top = 10.dp),
                            colors = colors
                        )
                        PolicyItem(
                            index = "3",
                            dotColor = colors.danger,
                            title = "No-show penalty",
                            description = "Not showing up: -25 points + temporary suspension",
                            modifier = Modifier.padding(top = 10.dp),
                            colors = colors
                        )

                        Divider(
                            color = colors.divider,
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 12.dp)
                        )

                        Text(
                            text = "Important: Maintaining a reliability score above 85 is required to continue using Wheels.",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Punctuality Rewards",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    colors = colors
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.rewardBackground)
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.rewardOverlay),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiEvents,
                                    contentDescription = "Rewards",
                                    tint = colors.onReward,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp)
                            ) {
                                Text(
                                    text = "Reward Points",
                                    color = colors.onReward,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Earn points for good behavior",
                                    color = colors.onReward.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = rewardPoints.toString(),
                                    color = colors.onReward,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "points",
                                    color = colors.onReward.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.rewardOverlay)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RewardRow(label = "On-time arrival", points = "+5 pts", colors = colors)
                                RewardRow(label = "Quick payment", points = "+3 pts", colors = colors)
                                RewardRow(label = "Positive review", points = "+10 pts", colors = colors)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReliabilityGauge(score: Int, colors: TrustFairnessColors) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(168.dp)) {
            val stroke = 12.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = colors.surfaceAlt,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = colors.success,
                startAngle = -90f,
                sweepAngle = 360f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text(
                text = "Score",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun QuickMetricChip(
    modifier: Modifier = Modifier,
    iconTint: Color,
    value: String,
    label: String,
    colors: TrustFairnessColors
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceAlt)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(iconTint)
                )
            }
            Text(
                text = value,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    colors: TrustFairnessColors
) {
    Text(
        text = title,
        color = colors.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun MetricCard(
    title: String,
    subtitle: String,
    tint: Color,
    colors: TrustFairnessColors,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colors.shadowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 14.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun PolicyItem(
    index: String,
    dotColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    colors: TrustFairnessColors
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(dotColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = colors.textSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RewardRow(label: String, points: String, colors: TrustFairnessColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.onReward.copy(alpha = 0.92f), fontSize = 12.sp)
        Text(text = points, color = colors.onReward, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

private data class TrustFairnessColors(
    val pageBackground: Color,
    val headerBackground: Color,
    val onHeader: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val shadowColor: Color,
    val success: Color,
    val info: Color,
    val warning: Color,
    val danger: Color,
    val rewardBackground: Color,
    val rewardOverlay: Color,
    val onReward: Color
)

@Composable
private fun trustFairnessColors(forceDark: Boolean): TrustFairnessColors {
    val isDark = if (forceDark) true else false
    return if (isDark) {
        TrustFairnessColors(
            pageBackground = Color(0xFF10141B),
            headerBackground = Color(0xFF1B2A3B),
            onHeader = Color(0xFFF4F8FF),
            surface = Color(0xFF18202B),
            surfaceAlt = Color(0xFF222D3A),
            textPrimary = Color(0xFFE6EDF7),
            textSecondary = Color(0xFF9EB0C6),
            divider = Color(0xFF2C3948),
            shadowColor = Color(0xFF000000).copy(alpha = 0.35f),
            success = Color(0xFF3DD8AA),
            info = Color(0xFF78A8EE),
            warning = Color(0xFFFFBE5C),
            danger = Color(0xFFFF7B7B),
            rewardBackground = Color(0xFF0FA07A),
            rewardOverlay = Color(0x33FFFFFF),
            onReward = Color(0xFFF5FFFC)
        )
    } else {
        TrustFairnessColors(
            pageBackground = Color(0xFFF7F9FC),
            headerBackground = Color(0xFF1A3A5C),
            onHeader = Color.White,
            surface = Color.White,
            surfaceAlt = Color(0xFFE8F0F9),
            textPrimary = Color(0xFF1A3A5C),
            textSecondary = Color(0xFF64748B),
            divider = Color(0xFFE5E9F2),
            shadowColor = Color(0xFF1A3A5C).copy(alpha = 0.12f),
            success = Color(0xFF00D9A3),
            info = Color(0xFF5B89C8),
            warning = Color(0xFFFFA726),
            danger = Color(0xFFFF5252),
            rewardBackground = Color(0xFF00C794),
            rewardOverlay = Color(0x29FFFFFF),
            onReward = Color.White
        )
    }
}
