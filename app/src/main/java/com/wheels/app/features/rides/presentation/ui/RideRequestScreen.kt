package com.wheels.app.features.rides.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestEvent
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestUiModel
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestUiState
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestViewModel

@Composable
fun RideRequestScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: RideRequestViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val ride = state.ride

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
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Back to rides",
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }
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
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            item {
                RideRequestHeader(
                    ride = ride,
                    onBack = { navController.popBackStack() }
                )
            }
            item { MapPreviewCard(ride = ride) }
            item { RouteDetailsCard(ride = ride) }
            item { DriverInfoCard(ride = ride) }
            item {
                SeatSelectionCard(
                    ride = ride,
                    selectedSeats = state.selectedSeats,
                    totalPrice = state.totalPrice,
                    onSeatSelected = { viewModel.onEvent(RideRequestEvent.SeatSelected(it)) }
                )
            }
            item { RideInformationCard(ride = ride) }
            item { SafetyNoteCard() }
        }

        RequestRideButton(
            totalPrice = state.totalPrice,
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { viewModel.onEvent(RideRequestEvent.RequestTapped) }
        )
    }

    if (state.showConfirmation) {
        ConfirmationDialog(
            ride = ride,
            selectedSeats = state.selectedSeats,
            totalPrice = state.totalPrice,
            onDismiss = { viewModel.onEvent(RideRequestEvent.ConfirmationDismissed) },
            onConfirm = {
                navController.navigate(
                    Destinations.BookingConfirmation.createRoute(
                        rideId = ride.id,
                        seats = state.selectedSeats
                    )
                ) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun RideRequestHeader(
    ride: RideRequestUiModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onBack),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to rides",
                tint = WheelsSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to rides",
                color = WheelsSurface.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Ride Details",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${ride.departureDate} at ${ride.departureTime}",
            color = WheelsSurface.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MapPreviewCard(ride: RideRequestUiModel) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .offset(y = (-10).dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFE8F0F9), WheelsBackground)))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = SecondaryBlue.copy(alpha = 0.18f)
                val verticalStops = listOf(size.width * 0.25f, size.width * 0.5f, size.width * 0.75f)
                val horizontalStops = listOf(size.height * 0.25f, size.height * 0.5f, size.height * 0.75f)
                verticalStops.forEach { x ->
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                }
                horizontalStops.forEach { y ->
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }

                drawLine(
                    color = SecondaryBlue.copy(alpha = 0.8f),
                    start = Offset(size.width * 0.20f, size.height * 0.82f),
                    end = Offset(size.width * 0.80f, size.height * 0.20f),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 28.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(SecondaryBlue)
                    .border(2.dp, WheelsSurface, CircleShape)
            )

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 32.dp)
                    .size(24.dp)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(999.dp),
                color = WheelsSurface,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = ride.distance,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RouteDetailsCard(ride: RideRequestUiModel) {
    InfoCard(title = "Route") {
        RouteRow(
            title = "Pickup",
            value = ride.origin,
            trailing = ride.departureTime,
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
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(0.dp)
                    .border(1.dp, Border, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${ride.estimatedDuration} trip",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        RouteRow(
            title = "Destination",
            value = ride.destination,
            trailing = ride.estimatedArrival,
            leading = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun DriverInfoCard(ride: RideRequestUiModel) {
    InfoCard(title = "Your Driver", trailingLabel = "View reviews") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ride.driver.initials,
                    color = WheelsSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ride.driver.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ride.driver.rating}",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = " (${ride.driver.ridesCount})",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified",
                        color = ElectricGreen,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${ride.driver.carModel} • ${ride.driver.carColor}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction(icon = Icons.Default.ChatBubble, contentDescription = "Message")
                SmallAction(icon = Icons.Default.Phone, contentDescription = "Phone")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TrustMetric(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                primary = "${ride.driver.reliabilityScore}%",
                secondary = "Reliable",
                tint = ElectricGreen,
                modifier = Modifier.weight(1f)
            )
            TrustMetric(
                icon = Icons.Default.AccessTime,
                primary = "${ride.driver.punctualityRate}%",
                secondary = "On-time",
                tint = SecondaryBlue,
                modifier = Modifier.weight(1f)
            )
            TrustMetric(
                icon = Icons.Default.Navigation,
                primary = ride.driver.responseTime,
                secondary = "Response",
                tint = SecondaryBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SeatSelectionCard(
    ride: RideRequestUiModel,
    selectedSeats: Int,
    totalPrice: Int,
    onSeatSelected: (Int) -> Unit
) {
    InfoCard(title = "Select Seats", trailingLabel = "${ride.availableSeats} available") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..ride.availableSeats).forEach { seat ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSeatSelected(seat) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedSeats == seat) PrimaryBlue else WheelsBackground,
                    border = if (selectedSeats == seat) null else BorderStroke(2.dp, Border)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$seat",
                            color = if (selectedSeats == seat) WheelsSurface else TextSecondary,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE8F0F9)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Price per seat", "$${ride.price}")
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(
                    label = "Total ($selectedSeats seat${if (selectedSeats > 1) "s" else ""})",
                    value = "$$totalPrice",
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun RideInformationCard(ride: RideRequestUiModel) {
    InfoCard(title = "Ride Information") {
        if (ride.amenities.isNotEmpty()) {
            Text(
                text = "Amenities",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ride.amenities.forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFE8F0F9)
                    ) {
                        Text(
                            text = amenity,
                            color = SecondaryBlue,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cancellation Policy",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ride.cancellationPolicy,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SafetyNoteCard() {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE8F0F9)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SecondaryBlue,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Safe Ride Guarantee",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All drivers are verified university students. Your safety is our priority.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RequestRideButton(
    totalPrice: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = WheelsSurface,
        shadowElevation = 12.dp
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricGreen,
                contentColor = WheelsSurface
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Request Ride • $$totalPrice",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    ride: RideRequestUiModel,
    selectedSeats: Int,
    totalPrice: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clickable(enabled = false) {},
                color = WheelsSurface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Border)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ElectricGreen.copy(alpha = 0.12f))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ElectricGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Confirm Your Request",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're requesting $selectedSeats seat${if (selectedSeats > 1) "s" else ""} with ${ride.driver.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = WheelsBackground
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SummaryRow("Ride to", ride.destination)
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRow("Departure", "${ride.departureDate}, ${ride.departureTime}")
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Border)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SummaryRow("Total", "$$totalPrice", emphasized = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Warning.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Warning.copy(alpha = 0.24f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "The driver will review your request. You'll be notified when accepted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onDismiss),
                            shape = RoundedCornerShape(16.dp),
                            color = WheelsSurface,
                            border = BorderStroke(2.dp, Border)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricGreen,
                                contentColor = WheelsSurface
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = "Confirm Request",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    trailingLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                if (trailingLabel != null) {
                    Text(
                        text = trailingLabel,
                        color = SecondaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        })
    }
}

@Composable
private fun RouteRow(
    title: String,
    value: String,
    trailing: String,
    leading: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.width(18.dp), contentAlignment = Alignment.TopCenter) {
            leading()
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = value,
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Text(
            text = trailing,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun SmallAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Surface(shape = CircleShape, color = Color(0xFFE8F0F9)) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = SecondaryBlue
            )
        }
    }
}

@Composable
private fun TrustMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: String,
    secondary: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = primary,
            color = PrimaryBlue,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(text = secondary, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (emphasized) PrimaryBlue else TextSecondary,
            style = if (emphasized) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )
        Text(
            text = value,
            color = PrimaryBlue,
            style = if (emphasized) {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            }
        )
    }
}
