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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.trust.domain.model.TrustScoreNotice
import com.wheels.app.core.trust.domain.model.TrustScoreNoticeType
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.rides.presentation.viewmodel.DriverPassengerUiModel
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideStatus
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideUiModel
import com.wheels.app.features.rides.presentation.viewmodel.PaymentStatusState
import com.wheels.app.features.rides.presentation.viewmodel.RidesEvent
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel

@Composable
fun ActiveRideManagementScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: RidesViewModel,
    rideId: String
) {
    val state by viewModel.uiState.collectAsState()
    val ride = state.driverRides.firstOrNull { it.id == rideId }
    var showEndConfirmation by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    val isActionInProgress = state.actionInProgressRideId == rideId
    val trustNotice = state.trustNotice

    if (ride == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WheelsBackground)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ride not found",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Go back to My Rides and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    if (ride.status == DriverRideStatus.COMPLETED) {
        CompletedRideContent(
            innerPadding = innerPadding,
            navController = navController,
            ride = ride
        )
        if (trustNotice != null) {
            TrustNoticeDialog(
                notice = trustNotice,
                onDismiss = {
                    val shouldPop = state.shouldPopAfterTrustNotice
                    viewModel.onEvent(RidesEvent.DismissTrustNotice)
                    if (shouldPop) {
                        navController.popBackStack()
                    }
                }
            )
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
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ActiveRideHeader(onBack = { navController.popBackStack() })
            }
            item {
                CurrentRideCard(ride = ride)
            }
            item {
                LiveRouteCard()
            }
            item {
                PassengersCard(
                    ride = ride,
                    passengers = ride.passengers
                )
            }
            item {
                EarningsCard(ride = ride)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = WheelsSurface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (ride.status == DriverRideStatus.PENDING) {
                    Button(
                        onClick = { viewModel.onEvent(RidesEvent.StartDriverRide(ride.id)) },
                        enabled = !isActionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (isActionInProgress) "Updating..." else "Start Ride",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isActionInProgress) { showCancelConfirmation = true },
                        shape = RoundedCornerShape(16.dp),
                        color = WheelsSurface,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Border)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isActionInProgress) "Updating..." else "Cancel Ride",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PrimaryBlue
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { showEndConfirmation = true },
                        enabled = !isActionInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (isActionInProgress) "Updating..." else "End Ride",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Destinations.GroupChat.route) },
                        shape = RoundedCornerShape(16.dp),
                        color = WheelsSurface,
                        border = androidx.compose.foundation.BorderStroke(2.dp, SecondaryBlue)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Contact Passengers",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SecondaryBlue
                            )
                        }
                    }
                }
            }
        }

        if (showEndConfirmation) {
            EndRideConfirmationDialog(
                onConfirm = {
                    viewModel.onEvent(RidesEvent.CompleteDriverRide(ride.id))
                    showEndConfirmation = false
                },
                onDismiss = { showEndConfirmation = false }
            )
        }

        if (showCancelConfirmation) {
            CancelRideConfirmationDialog(
                onConfirm = {
                    viewModel.onEvent(RidesEvent.CancelDriverRide(ride.id))
                    showCancelConfirmation = false
                },
                onDismiss = { showCancelConfirmation = false }
            )
        }

        if (trustNotice != null) {
            TrustNoticeDialog(
                notice = trustNotice,
                onDismiss = {
                    val shouldPop = state.shouldPopAfterTrustNotice
                    viewModel.onEvent(RidesEvent.DismissTrustNotice)
                    if (shouldPop) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@Composable
private fun ActiveRideHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
            .padding(20.dp)
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
                text = "Back to My Rides",
                color = WheelsSurface.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Active Ride Management",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Coordinate your current ride and manage passengers",
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.84f)
        )
    }
}

@Composable
private fun CurrentRideCard(ride: DriverRideUiModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Ride",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                RideStatusBadge(status = ride.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WheelsBackground)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = ride.origin,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(16.dp)
                        .width(0.dp)
                        .border(1.dp, Border, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ride.destination,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ManagementMetricTile(
                    title = "Departure",
                    value = ride.time,
                    modifier = Modifier.weight(1f)
                )
                ManagementMetricTile(
                    title = "Est. Arrival",
                    value = ride.estimatedArrival,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiveRouteCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "[ LIVE ROUTE TRACKING ]",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PassengersCard(
    ride: DriverRideUiModel,
    passengers: List<DriverPassengerUiModel>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Passengers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Text(
                    text = "${passengers.size} / ${ride.totalSeats}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                passengers.forEach { passenger ->
                    PassengerRow(passenger = passenger)
                }
            }
        }
    }
}

@Composable
private fun PassengerRow(passenger: DriverPassengerUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = passenger.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
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
                        text = "${passenger.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Seat ${passenger.seat}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ElectricGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = passenger.status,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun CompletedPassengerRow(passenger: DriverPassengerUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = passenger.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seat ${passenger.seat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        PaymentStatusBadge(status = passenger.paymentStatus)
    }
}

@Composable
private fun EarningsCard(ride: DriverRideUiModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Earnings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(14.dp))
            EarningsRow(label = "Price per seat", value = "$${ride.pricePerSeat}")
            Spacer(modifier = Modifier.height(8.dp))
            EarningsRow(label = "Passengers", value = "× ${ride.passengers.size}")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL EARNINGS",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = "$${ride.totalEarnings}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Payment will be released after ride completion",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EarningsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue)
    }
}

@Composable
private fun ManagementMetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WheelsBackground)
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
    }
}

@Composable
private fun RideStatusBadge(status: DriverRideStatus) {
    val (label, color) = when (status) {
        DriverRideStatus.PENDING -> "Pending" to Color(0xFFF59E0B)
        DriverRideStatus.ACTIVE -> "Active" to ElectricGreen
        DriverRideStatus.COMPLETED -> "Completed" to PrimaryBlue
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

@Composable
private fun PaymentStatusBadge(status: PaymentStatusState) {
    val (label, bgColor, textColor) = when (status) {
        PaymentStatusState.PAID -> Triple("Paid", Color(0xFFDCFCE7), ElectricGreen)
        PaymentStatusState.PENDING -> Triple("Pending", Color(0xFFFEF3C7), Color(0xFFF59E0B))
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
private fun EndRideConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WheelsSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "End Ride?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This will complete the ride and trigger payment release. Passengers will be able to rate you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WheelsBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Actions on completion:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "• Release payments to your account", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "• Enable passenger reviews", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "• Close ride status", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "Confirm & End Ride",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismiss),
                    shape = RoundedCornerShape(16.dp),
                    color = WheelsSurface,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Border)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelRideConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WheelsSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Cancel Ride?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This cancelation may reduce your reliability score depending on how close it is to departure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "Confirm Cancellation",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismiss),
                    shape = RoundedCornerShape(16.dp),
                    color = WheelsSurface,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Border)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Go Back",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustNoticeDialog(
    notice: TrustScoreNotice,
    onDismiss: () -> Unit
) {
    val accentColor = if (notice.type == TrustScoreNoticeType.ERROR) {
        Color(0xFFDC2626)
    } else {
        ElectricGreen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WheelsSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (notice.newScore != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WheelsBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current trust score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${notice.newScore}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedRideContent(
    innerPadding: PaddingValues,
    navController: NavController,
    ride: DriverRideUiModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WheelsSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WheelsSurface,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Ride Completed",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Passenger payments may now be paid or still pending while the settlement finishes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WheelsBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            EarningsRow(label = "Earnings", value = "$${ride.totalEarnings}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${ride.passengers.size} passengers × $${ride.pricePerSeat}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WheelsSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Passengers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ride.passengers.forEach { passenger ->
                            CompletedPassengerRow(passenger = passenger)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "Rate Passengers",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.popBackStack() },
                shape = RoundedCornerShape(16.dp),
                color = WheelsSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, Border)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to My Rides",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Destinations.Home.route) },
                shape = RoundedCornerShape(16.dp),
                color = PrimaryBlue
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = WheelsSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = WheelsSurface
                        )
                    }
                }
            }
        }
    }
}
