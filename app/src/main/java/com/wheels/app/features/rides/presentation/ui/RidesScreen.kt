package com.wheels.app.features.rides.presentation.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.features.rides.presentation.viewmodel.RideCardUiModel
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideUiModel
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideStatus
import com.wheels.app.features.rides.presentation.viewmodel.DriverRidesTab
import com.wheels.app.features.rides.presentation.viewmodel.RidesEvent
import com.wheels.app.features.rides.presentation.viewmodel.RidesUiState
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel
import java.util.Calendar

@Composable
fun RidesScreen(
    innerPadding: PaddingValues,
    viewModel: RidesViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    if (activeRole == UserRole.DRIVER) {
        DriverCreateRideScreen(
            state = state,
            innerPadding = innerPadding,
            onBack = { navController.navigate(Destinations.Home.route) },
            onOriginChanged = { viewModel.onEvent(RidesEvent.DriverOriginChanged(it)) },
            onDestinationChanged = { viewModel.onEvent(RidesEvent.DriverDestinationChanged(it)) },
            onDateChanged = { viewModel.onEvent(RidesEvent.DriverDateChanged(it)) },
            onTimeChanged = { viewModel.onEvent(RidesEvent.DriverTimeChanged(it)) },
            onIncreaseSeats = { viewModel.onEvent(RidesEvent.DriverIncreaseSeats) },
            onDecreaseSeats = { viewModel.onEvent(RidesEvent.DriverDecreaseSeats) },
            onPriceChanged = { viewModel.onEvent(RidesEvent.DriverPriceChanged(it)) },
            onCarModelChanged = { viewModel.onEvent(RidesEvent.DriverCarModelChanged(it)) },
            onLicensePlateChanged = { viewModel.onEvent(RidesEvent.DriverLicensePlateChanged(it)) },
            onDescriptionChanged = { viewModel.onEvent(RidesEvent.DriverDescriptionChanged(it)) },
            onDriverTabSelected = { viewModel.onEvent(RidesEvent.DriverTabChanged(it)) },
            onMyRideSelected = { rideId ->
                navController.navigate(Destinations.ActiveRideManagement.createRoute(rideId))
            },
            onPublishRide = {
                viewModel.onEvent(RidesEvent.PublishRide)
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 104.dp)
    ) {
        item {
            RidesHeader(
                onBack = { navController.navigate(Destinations.Home.route) }
            )
        }

        item {
            SearchAndFiltersSection(
                state = state,
                onSearchChanged = { viewModel.onEvent(RidesEvent.SearchChanged(it)) },
                onToggleFilters = {
                    viewModel.onEvent(RidesEvent.FiltersExpandedChanged(!state.showFilters))
                },
                onAreaSelected = { viewModel.onEvent(RidesEvent.AreaSelected(it)) },
                onPriceChanged = { viewModel.onEvent(RidesEvent.MaxPriceChanged(it)) },
                onRatingSelected = { viewModel.onEvent(RidesEvent.MinRatingSelected(it)) },
                onClearRating = { viewModel.onEvent(RidesEvent.ClearRatingFilter) }
            )
        }

        item {
            SmartSuggestionCard(
                onClick = { viewModel.onEvent(RidesEvent.ApplySuggestedDestination) }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Drivers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
                Text(
                    text = "${state.filteredRides.size} rides",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(state.filteredRides, key = { it.id }) { ride ->
            RideCard(
                ride = ride,
                onRequest = {
                    navController.navigate(Destinations.RideRequest.createRoute(ride.id))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DriverCreateRideScreen(
    state: RidesUiState,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onOriginChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onIncreaseSeats: () -> Unit,
    onDecreaseSeats: () -> Unit,
    onPriceChanged: (String) -> Unit,
    onCarModelChanged: (String) -> Unit,
    onLicensePlateChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDriverTabSelected: (DriverRidesTab) -> Unit,
    onMyRideSelected: (String) -> Unit,
    onPublishRide: () -> Unit
) {
    val context = LocalContext.current

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
            item {
                CreateRideHeader(onBack = onBack)
            }

            item {
                DriverTabSwitcher(
                    selectedTab = state.driverSelectedTab,
                    onTabSelected = onDriverTabSelected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            if (state.driverSelectedTab == DriverRidesTab.CREATE_RIDE) {
                item {
                    EarningsPreviewCard(
                        estimatedEarnings = state.estimatedEarnings,
                        totalSeats = state.totalSeats,
                        pricePerSeat = state.pricePerSeat
                    )
                }

                item {
                    FormSection(title = "Route Details") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FormTextField(
                                value = state.origin,
                                onValueChange = onOriginChanged,
                                label = "Pickup Location",
                                placeholder = "e.g., Campus Uniandes - Main Gate",
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryBlue)
                                    )
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .height(28.dp)
                                    .width(0.dp)
                                    .border(1.dp, Border, RoundedCornerShape(2.dp))
                            )

                            FormTextField(
                                value = state.destination,
                                onValueChange = onDestinationChanged,
                                label = "Destination",
                                placeholder = "e.g., Centro Comercial Andino",
                                leadingIcon = Icons.Default.LocationOn
                            )
                        }
                    }
                }

                item {
                    FormSection(title = "Schedule") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FormTextField(
                                value = state.date,
                                onValueChange = onDateChanged,
                                label = "Date",
                                placeholder = "YYYY-MM-DD",
                                leadingIcon = Icons.Default.CalendarMonth,
                                readOnly = true,
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    val datePickerDialog = DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            onDateChanged(
                                                String.format(
                                                    "%04d-%02d-%02d",
                                                    year,
                                                    month + 1,
                                                    dayOfMonth
                                                )
                                            )
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    datePickerDialog.datePicker.minDate = calendar.timeInMillis
                                    datePickerDialog.show()
                                }
                            )
                            FormTextField(
                                value = state.time,
                                onValueChange = onTimeChanged,
                                label = "Departure Time",
                                placeholder = "HH:MM",
                                leadingIcon = Icons.Default.Schedule,
                                readOnly = true,
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            onTimeChanged(String.format("%02d:%02d", hourOfDay, minute))
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                            )
                        }
                    }
                }

                item {
                    FormSection(title = "Capacity & Pricing") {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column {
                                SectionLabel(
                                    label = "Available Seats",
                                    icon = Icons.Default.People
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    SeatStepperButton(
                                        icon = Icons.Default.Remove,
                                        onClick = onDecreaseSeats
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${state.totalSeats}",
                                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "seats available",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    SeatStepperButton(
                                        icon = Icons.Default.Add,
                                        onClick = onIncreaseSeats
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Border)
                            )

                            Column {
                                FormTextField(
                                    value = state.pricePerSeat,
                                    onValueChange = { newValue ->
                                        onPriceChanged(newValue.filter { it.isDigit() })
                                    },
                                    label = "Price per Seat",
                                    placeholder = "3500",
                                    leadingIcon = Icons.Default.Info,
                                    prefix = "$",
                                    keyboardType = KeyboardType.Number
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Suggested price based on distance: \$3,000 - \$5,000",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    FormSection(title = "Vehicle Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            FormTextField(
                                value = state.carModel,
                                onValueChange = onCarModelChanged,
                                label = "Car Model",
                                placeholder = "e.g., Toyota Corolla 2020",
                                leadingIcon = Icons.Default.DirectionsCar
                            )
                            FormTextField(
                                value = state.licensePlate,
                                onValueChange = onLicensePlateChanged,
                                label = "License Plate",
                                placeholder = "ABC-123",
                                leadingIcon = Icons.Default.VerifiedUser
                            )
                        }
                    }
                }

                item {
                    FormSection(title = "Additional Information (Optional)") {
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = onDescriptionChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Add any special notes: AC available, music preferences, stops along the way, etc.")
                            },
                            shape = RoundedCornerShape(16.dp),
                            minLines = 4,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0F9))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = SecondaryBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Before Publishing",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "\u2022 Make sure your vehicle is clean and in good condition",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\u2022 Arrive on time to maintain your reliability score",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\u2022 Cancellations may affect your driver rating",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\u2022 Be respectful to all passengers",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                item {
                    MyRidesSummary(
                        rideCount = state.driverRides.size,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(state.driverRides, key = { it.id }) { ride ->
                    DriverRideCard(
                        ride = ride,
                        onClick = {
                            onMyRideSelected(ride.id)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (state.driverSelectedTab == DriverRidesTab.CREATE_RIDE) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = WheelsSurface,
                shadowElevation = 12.dp,
                tonalElevation = 2.dp
            ) {
                Button(
                    onClick = onPublishRide,
                    enabled = state.canPublishRide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    content = {
                        Text(
                            text = "Publish Ride",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DriverTabSwitcher(
    selectedTab: DriverRidesTab,
    onTabSelected: (DriverRidesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F0F9))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DriverTabButton(
            text = "Create Ride",
            selected = selectedTab == DriverRidesTab.CREATE_RIDE,
            onClick = { onTabSelected(DriverRidesTab.CREATE_RIDE) },
            modifier = Modifier.weight(1f)
        )
        DriverTabButton(
            text = "My Rides",
            selected = selectedTab == DriverRidesTab.MY_RIDES,
            onClick = { onTabSelected(DriverRidesTab.MY_RIDES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DriverTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) WheelsSurface else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) PrimaryBlue else TextSecondary
            )
        }
    }
}

@Composable
private fun MyRidesSummary(
    rideCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElectricGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Your Published Rides",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WheelsSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$rideCount rides ordered by date and departure time",
                style = MaterialTheme.typography.bodyMedium,
                color = WheelsSurface.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DriverRideCard(
    ride: DriverRideUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ride.formattedSchedule,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DriverRideStatusChip(status = ride.status)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${ride.carModel} • ${ride.licensePlate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "$${ride.totalEarnings}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ElectricGreen
                )
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
                            .background(SecondaryBlue)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    icon = Icons.Default.Schedule,
                    primary = ride.time,
                    secondary = ride.date,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    icon = Icons.Default.EventSeat,
                    primary = "${ride.totalSeats}",
                    secondary = "seats",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    icon = Icons.Default.Info,
                    primary = "$${ride.pricePerSeat}",
                    secondary = "per seat",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DriverRideStatusChip(status: DriverRideStatus) {
    val (label, backgroundColor, textColor) = when (status) {
        DriverRideStatus.PENDING -> Triple("Pending", Color(0xFFFEF3C7), Color(0xFFF59E0B))
        DriverRideStatus.ACTIVE -> Triple("Active", Color(0xFFD1FAE5), ElectricGreen)
        DriverRideStatus.COMPLETED -> Triple("Completed", Color(0xFFE2E8F0), PrimaryBlue)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
private fun CreateRideHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(PrimaryBlue, Color(0xFF2D5280))))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create a Ride",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Publish your ride and earn money",
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.84f)
        )
    }
}

@Composable
private fun EarningsPreviewCard(
    estimatedEarnings: Int,
    totalSeats: Int,
    pricePerSeat: String
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElectricGreen)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estimated Earnings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WheelsSurface.copy(alpha = 0.84f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${estimatedEarnings}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = WheelsSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalSeats seats × \$${pricePerSeat.ifBlank { "0" }} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = WheelsSurface.copy(alpha = 0.84f)
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(WheelsSurface.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = WheelsSurface
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WheelsSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SectionLabel(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SecondaryBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = PrimaryBlue
        )
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                leadingContent != null -> leadingContent()
                leadingIcon != null -> Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SecondaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = PrimaryBlue
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                prefix = if (prefix != null) ({ Text(prefix) }) else null,
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                readOnly = readOnly,
                enabled = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
            
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                )
            }
        }
    }
}

@Composable
private fun SeatStepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(2.dp, Border),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue
        )
    }
}

@Composable
private fun RidesHeader(onBack: () -> Unit) {
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
            text = "Find a Ride",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = WheelsSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Available rides from your university",
            style = MaterialTheme.typography.bodyMedium,
            color = WheelsSurface.copy(alpha = 0.82f)
        )
    }
}

@Composable
private fun SearchAndFiltersSection(
    state: RidesUiState,
    onSearchChanged: (String) -> Unit,
    onToggleFilters: () -> Unit,
    onAreaSelected: (String) -> Unit,
    onPriceChanged: (Float) -> Unit,
    onRatingSelected: (Double) -> Unit,
    onClearRating: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WheelsSurface)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Where are you going?") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleFilters),
            shape = RoundedCornerShape(16.dp),
            color = WheelsBackground,
            border = BorderStroke(2.dp, Border)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filters",
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filters",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (state.showFilters) {
            Spacer(modifier = Modifier.height(12.dp))
            FilterPanel(
                state = state,
                onAreaSelected = onAreaSelected,
                onPriceChanged = onPriceChanged,
                onRatingSelected = (onRatingSelected),
                onClearRating = onClearRating
            )
        }
    }
}

@Composable
private fun FilterPanel(
    state: RidesUiState,
    onAreaSelected: (String) -> Unit,
    onPriceChanged: (Float) -> Unit,
    onRatingSelected: (Double) -> Unit,
    onClearRating: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WheelsBackground)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Destination Area",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableAreas.take(3).forEach { area ->
                FilterChip(
                    label = area,
                    selected = state.selectedArea == area,
                    onClick = { onAreaSelected(area) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableAreas.drop(3).forEach { area ->
                FilterChip(
                    label = area,
                    selected = state.selectedArea == area,
                    onClick = { onAreaSelected(area) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Max Price: $${state.maxPrice.toInt()}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
        Slider(
            value = state.maxPrice,
            onValueChange = onPriceChanged,
            valueRange = 0f..10000f
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Min Rating",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableRatings.forEach { rating ->
                FilterChip(
                    label = "$rating+",
                    selected = state.selectedMinRating == rating,
                    onClick = { onRatingSelected(rating) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.selectedMinRating != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Clear rating filter",
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.clickable(onClick = onClearRating)
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SecondaryBlue else WheelsSurface,
        border = BorderStroke(2.dp, if (selected) SecondaryBlue else Border)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) WheelsSurface else PrimaryBlue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun SmartSuggestionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElectricGreen
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WheelsSurface.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = WheelsSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Suggestion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = WheelsSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on your schedule, you usually go to Centro around 2:30 PM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WheelsSurface.copy(alpha = 0.92f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = WheelsSurface.copy(alpha = 0.18f),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Text(
                        text = "View matching rides",
                        color = WheelsSurface,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RideCard(
    ride: RideCardUiModel,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (ride.isHabitRide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Matches your usual schedule",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ElectricGreen
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ride.initials,
                        color = WheelsSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ride.driver,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                            text = "${ride.rating}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = PrimaryBlue
                        )
                        Text(
                            text = " (${ride.ridesCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ride.reliabilityScore}% reliable",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = ElectricGreen
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ride.compactPrice,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Text(
                        text = "per seat",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
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
                            .background(SecondaryBlue)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    icon = Icons.Default.Schedule,
                    primary = ride.departureTime,
                    secondary = ride.estimatedDuration,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    icon = Icons.Default.People,
                    primary = "${ride.availableSeats}/${ride.totalSeats}",
                    secondary = "seats left",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    primary = "${ride.punctualityRate}%",
                    secondary = "on-time",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActionButton(
                text = "Request",
                emphasized = true,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = onRequest,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: String,
    secondary: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = primary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PrimaryBlue
        )
        Text(
            text = secondary,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (emphasized) ElectricGreen else WheelsSurface,
        border = if (emphasized) null else BorderStroke(2.dp, SecondaryBlue)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                color = if (emphasized) WheelsSurface else SecondaryBlue,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = WheelsSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
