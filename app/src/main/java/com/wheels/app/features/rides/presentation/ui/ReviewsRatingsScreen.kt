package com.wheels.app.features.rides.presentation.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface

private data class ReviewItem(
    val id: Int,
    val reviewer: String,
    val role: String,
    val rating: Int,
    val date: String,
    val comment: String,
    val tags: List<String>,
    val verified: Boolean,
    val paymentOnTime: Boolean
)

private data class DriverStats(
    val overallRating: Double,
    val totalReviews: Int,
    val ratingBreakdown: Map<Int, Int>,
    val punctualityScore: Int,
    val paymentReliability: Int,
    val verifiedStudent: Boolean
)

private val mockReviews = listOf(
    ReviewItem(
        id = 1,
        reviewer = "Maria Gonzalez",
        role = "Passenger",
        rating = 5,
        date = "2 days ago",
        comment = "Amazing driver! Very punctual and professional. The ride was smooth and comfortable.",
        tags = listOf("Punctual", "Safe driving", "Friendly"),
        verified = true,
        paymentOnTime = true
    ),
    ReviewItem(
        id = 2,
        reviewer = "Carlos Ruiz",
        role = "Passenger",
        rating = 5,
        date = "5 days ago",
        comment = "Great experience! Easy payment and good conversation. Will definitely ride again.",
        tags = listOf("Good communication", "Clean car"),
        verified = true,
        paymentOnTime = true
    ),
    ReviewItem(
        id = 3,
        reviewer = "Ana Martinez",
        role = "Driver",
        rating = 5,
        date = "1 week ago",
        comment = "Perfect passenger! On time, respectful, and paid immediately after the ride.",
        tags = listOf("Punctual", "Respectful", "Quick payment"),
        verified = true,
        paymentOnTime = true
    ),
    ReviewItem(
        id = 4,
        reviewer = "Juan Lopez",
        role = "Passenger",
        rating = 4,
        date = "1 week ago",
        comment = "Good ride overall. Minor delay but good communication throughout.",
        tags = listOf("Good communication"),
        verified = true,
        paymentOnTime = true
    )
)

private val driverStats = DriverStats(
    overallRating = 4.9,
    totalReviews = 124,
    ratingBreakdown = mapOf(5 to 98, 4 to 20, 3 to 4, 2 to 1, 1 to 1),
    punctualityScore = 96,
    paymentReliability = 99,
    verifiedStudent = true
)

@Composable
fun ReviewsRatingsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    driverName: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ReviewsHeader(driverName = driverName, onBack = { navController.popBackStack() })
        }

        item {
            RatingSummaryCard(driverName = driverName)
        }

        item {
            FiltersRow()
        }

        items(mockReviews, key = { it.id }) { review ->
            ReviewCard(review = review)
        }

        item {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Load more reviews")
            }
        }
    }
}

@Composable
private fun ReviewsHeader(driverName: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = WheelsSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back",
                color = WheelsSurface.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Reviews and Ratings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$driverName profile",
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.82f)
        )
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun RatingSummaryCard(driverName: String) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .offset(y = (-25).dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${driverStats.overallRating}",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFA726),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${driverStats.totalReviews} reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 4, 3, 2, 1).forEach { stars ->
                        val votes = driverStats.ratingBreakdown[stars] ?: 0
                        val progress = votes.toFloat() / driverStats.totalReviews.toFloat()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$stars",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFE8F0F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .height(6.dp)
                                        .background(Color(0xFFFFA726))
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$votes",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                    }
                }
            }

            if (driverStats.verifiedStudent) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElectricGreen.copy(alpha = 0.10f))
                        .border(1.dp, ElectricGreen.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Verified Student",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                        Text(
                            text = "University email verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBox(
                    icon = Icons.Default.AccessTime,
                    label = "Punctuality",
                    value = "${driverStats.punctualityScore}%",
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    icon = Icons.Default.CheckCircle,
                    label = "Payment",
                    value = "${driverStats.paymentReliability}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = WheelsBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = SecondaryBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = ElectricGreen, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun FiltersRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.clickable { },
            shape = RoundedCornerShape(12.dp),
            color = WheelsSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, Border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "All reviews", color = PrimaryBlue, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
            }
        }

        Surface(
            modifier = Modifier.clickable { },
            shape = RoundedCornerShape(12.dp),
            color = WheelsSurface,
            border = androidx.compose.foundation.BorderStroke(2.dp, Border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Most recent", color = PrimaryBlue, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = review.reviewer.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
                    Text(
                        text = initials,
                        color = WheelsSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.reviewer,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                        if (review.verified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${review.role} - ${review.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(review.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA726),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                review.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFE8F0F9)
                    ) {
                        Text(
                            text = tag,
                            color = SecondaryBlue,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (review.paymentOnTime) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "On-time payment verified",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ElectricGreen
                    )
                }
            }
        }
    }
}
