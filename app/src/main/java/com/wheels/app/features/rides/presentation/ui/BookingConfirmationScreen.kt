package com.wheels.app.features.rides.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.Warning
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestUiModel

@Composable
fun BookingConfirmationScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    ride: RideRequestUiModel?,
    selectedSeats: Int
) {
    if (ride == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WheelsBackground)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Booking not found", color = TextSecondary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 132.dp)
        ) {
            item { SuccessHeader() }
            item { PendingStatusCard() }
            item { BookingDetailsSection(ride = ride) }
            item { PaymentSummarySection(ride = ride, selectedSeats = selectedSeats) }
            item { CancellationPolicyCard() }
            item { WhatHappensNextCard() }
        }

        ConfirmationBottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            onMessage = {},
            onCall = {},
            onBackHome = {
                navController.navigate(Destinations.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun SuccessHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(ElectricGreen, Color(0xFF00C794))))
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(WheelsSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ElectricGreen,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Ride Requested!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Waiting for driver confirmation",
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun PendingStatusCard() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .offset(y = (-20).dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Warning)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pending Driver Acceptance",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "The driver will review your request and respond within the next few minutes. You'll receive a notification once accepted.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BookingDetailsSection(ride: RideRequestUiModel) {
    SectionTitle("Booking Details")
    CardBlock {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ride.driver.initials,
                    color = WheelsSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = ride.driver.name,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ride.driver.rating} • ${ride.driver.carModel.substringBeforeLast(" ")}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        DividerSpace()

        RouteInfoLine(
            title = "Pickup",
            value = ride.origin,
            subtitle = "Main entrance pickup point",
            leading = {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SecondaryBlue)
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .height(20.dp)
                .width(1.dp)
                .background(Border)
        )
        Spacer(modifier = Modifier.height(8.dp))

        RouteInfoLine(
            title = "Destination",
            value = ride.destination,
            leading = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        DividerSpace()

        SummaryIconRow(
            icon = Icons.Default.AccessTime,
            label = "Departure",
            value = "${ride.departureDate}, ${ride.departureTime}"
        )
        Spacer(modifier = Modifier.height(10.dp))
        SummaryIconRow(
            icon = Icons.Default.CalendarToday,
            label = "Duration",
            value = "~${ride.estimatedDuration}"
        )
    }
}

@Composable
private fun PaymentSummarySection(
    ride: RideRequestUiModel,
    selectedSeats: Int
) {
    val totalPrice = ride.price * selectedSeats

    SectionTitle("Payment Summary")
    CardBlock {
        SummaryRowSimple("Ride fare ($selectedSeats seat${if (selectedSeats > 1) "s" else ""})", "$$totalPrice")
        Spacer(modifier = Modifier.height(10.dp))
        SummaryRowSimple("Platform fee", "$0")
        DividerSpace()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Total",
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$$totalPrice",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ElectricGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Paid",
                        color = ElectricGreen,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun CancellationPolicyCard() {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Warning.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.20f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Cancellation Policy",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Free cancellation up to 1 hour before departure. 50% penalty after that. Late cancellations may affect your reliability score.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WhatHappensNextCard() {
    SectionTitle("What Happens Next?")
    CardBlock {
        NextStep(number = "1", title = "Driver Reviews Request", subtitle = "Carlos will review your booking details")
        Spacer(modifier = Modifier.height(16.dp))
        NextStep(number = "2", title = "Confirmation Notification", subtitle = "You'll receive an alert when accepted")
        Spacer(modifier = Modifier.height(16.dp))
        NextStep(number = "3", title = "Pickup Day", subtitle = "Arrive 5 minutes early at the pickup point")
    }
}

@Composable
private fun ConfirmationBottomActions(
    onMessage: () -> Unit,
    onCall: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = WheelsSurface,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryActionButton(
                    text = "Message",
                    icon = Icons.Default.ChatBubble,
                    onClick = onMessage,
                    modifier = Modifier.weight(1f)
                )
                SecondaryActionButton(
                    text = "Call",
                    icon = Icons.Default.Phone,
                    onClick = onCall,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WheelsSurface
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE8F0F9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = icon, contentDescription = null, tint = SecondaryBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = SecondaryBlue,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun DividerSpace() {
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Border)
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun RouteInfoLine(
    title: String,
    value: String,
    subtitle: String? = null,
    leading: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.TopCenter) {
            leading()
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = value,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            if (subtitle != null) {
                Text(text = subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SummaryIconRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun SummaryRowSimple(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NextStep(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F0F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Text(text = subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
