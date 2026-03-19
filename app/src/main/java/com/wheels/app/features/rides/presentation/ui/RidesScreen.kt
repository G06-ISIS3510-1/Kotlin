package com.wheels.app.features.rides.presentation.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.wheels.app.features.rides.presentation.viewmodel.RideCardUiModel
import com.wheels.app.features.rides.presentation.viewmodel.RidesEvent
import com.wheels.app.features.rides.presentation.viewmodel.RidesUiState
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel

@Composable
fun RidesScreen(
    innerPadding: PaddingValues,
    viewModel: RidesViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()

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
                onOpenReviews = {
                    navController.navigate(
                        Destinations.ReviewsRatings.createRoute(
                            driverName = ride.driver,
                            origin = Destinations.Rides.route
                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
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
                onRatingSelected = onRatingSelected,
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
    onOpenReviews: () -> Unit,
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
                        .background(Brush.linearGradient(listOf(Color(0xFF5B89C8), PrimaryBlue)))
                        .clickable(onClick = onOpenReviews),
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
                        color = PrimaryBlue,
                        modifier = Modifier.clickable(onClick = onOpenReviews)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenReviews)
                    ) {
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
