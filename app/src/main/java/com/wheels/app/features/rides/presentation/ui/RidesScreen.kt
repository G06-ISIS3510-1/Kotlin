package com.wheels.app.features.rides.presentation.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.wheels.app.core.navigation.Destinations
import com.wheels.app.core.session.UserRole
import com.wheels.app.core.ui.theme.Border
import com.wheels.app.core.ui.theme.ElectricGreen
import com.wheels.app.core.ui.theme.GradientHeaderPrimaryContent
import com.wheels.app.core.ui.theme.GradientHeaderSecondaryContent
import com.wheels.app.core.ui.theme.PrimaryBlue
import com.wheels.app.core.ui.theme.SecondaryBlue
import com.wheels.app.core.ui.theme.TextSecondary
import com.wheels.app.core.ui.theme.WheelsBackground
import com.wheels.app.core.ui.theme.WheelsSurface
import com.wheels.app.core.ui.theme.gradientHeaderBrush
import com.wheels.app.features.rides.domain.model.BehavioralNudge
import com.wheels.app.features.rides.domain.model.CreateRideDraftSummary
import com.wheels.app.features.rides.domain.model.PendingRideActionType
import com.wheels.app.features.rides.presentation.model.LocationSuggestion
import com.wheels.app.features.rides.presentation.model.RideLocationField
import com.wheels.app.features.rides.presentation.ui.components.LocationAutocompleteField
import com.wheels.app.features.rides.presentation.viewmodel.RideCardUiModel
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideUiModel
import com.wheels.app.features.rides.presentation.viewmodel.DriverRideStatus
import com.wheels.app.features.rides.presentation.viewmodel.DriverRidesTab
import com.wheels.app.features.rides.presentation.viewmodel.RideActionInfoNotice
import com.wheels.app.features.rides.presentation.viewmodel.RidesEvent
import com.wheels.app.features.rides.presentation.viewmodel.RidesUiState
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RidesScreen(
    innerPadding: PaddingValues,
    viewModel: RidesViewModel,
    navController: NavController
) {
    val state by viewModel.uiState.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    LaunchedEffect(navController, activeRole) {
        if (activeRole != UserRole.PASSENGER) {
            viewModel.onEvent(RidesEvent.ClearNearbyRides)
            return@LaunchedEffect
        }

        val homeBackStackEntry = runCatching {
            navController.getBackStackEntry(Destinations.Home.route)
        }.getOrNull() ?: return@LaunchedEffect

        val savedStateHandle = homeBackStackEntry.savedStateHandle
        val nearbyRequested = savedStateHandle.get<Boolean>(Destinations.RIDES_NEARBY_REQUESTED_KEY) == true
        if (!nearbyRequested) {
            viewModel.onEvent(RidesEvent.ClearNearbyRides)
            return@LaunchedEffect
        }

        viewModel.onEvent(
            RidesEvent.ApplyNearbyRides(
                savedStateHandle.get<String>(Destinations.RIDES_NEARBY_LOCATION_NAME_KEY)
            )
        )
        savedStateHandle.remove<Boolean>(Destinations.RIDES_NEARBY_REQUESTED_KEY)
        savedStateHandle.remove<String>(Destinations.RIDES_NEARBY_LOCATION_NAME_KEY)
    }

    if (activeRole == UserRole.DRIVER) {
        Box(modifier = Modifier.fillMaxSize()) {
            DriverCreateRideScreen(
                state = state,
                innerPadding = innerPadding,
                onBack = { navController.navigate(Destinations.Home.route) },
                onOriginChanged = {
                    viewModel.onEvent(RidesEvent.DriverLocationQueryChanged(RideLocationField.ORIGIN, it))
                },
                onOriginFieldFocused = {
                    viewModel.onEvent(RidesEvent.DriverLocationFieldFocused(RideLocationField.ORIGIN))
                },
                onOriginSuggestionSelected = {
                    viewModel.onEvent(
                        RidesEvent.DriverLocationSuggestionSelected(RideLocationField.ORIGIN, it)
                    )
                },
                onUseCurrentOriginClicked = {
                    viewModel.onEvent(RidesEvent.DriverUseCurrentLocation(RideLocationField.ORIGIN))
                },
                onDestinationChanged = {
                    viewModel.onEvent(RidesEvent.DriverLocationQueryChanged(RideLocationField.DESTINATION, it))
                },
                onDestinationFieldFocused = {
                    viewModel.onEvent(RidesEvent.DriverLocationFieldFocused(RideLocationField.DESTINATION))
                },
                onDestinationSuggestionSelected = {
                    viewModel.onEvent(
                        RidesEvent.DriverLocationSuggestionSelected(RideLocationField.DESTINATION, it)
                    )
                },
                onUseCurrentDestinationClicked = {
                    viewModel.onEvent(RidesEvent.DriverUseCurrentLocation(RideLocationField.DESTINATION))
                },
                onDateChanged = { viewModel.onEvent(RidesEvent.DriverDateChanged(it)) },
                onTimeChanged = { viewModel.onEvent(RidesEvent.DriverTimeChanged(it)) },
                onIncreaseSeats = { viewModel.onEvent(RidesEvent.DriverIncreaseSeats) },
                onDecreaseSeats = { viewModel.onEvent(RidesEvent.DriverDecreaseSeats) },
                onPriceChanged = { viewModel.onEvent(RidesEvent.DriverPriceChanged(it)) },
                onCarModelChanged = { viewModel.onEvent(RidesEvent.DriverCarModelChanged(it)) },
                onLicensePlateChanged = { viewModel.onEvent(RidesEvent.DriverLicensePlateChanged(it)) },
                onDescriptionChanged = { viewModel.onEvent(RidesEvent.DriverDescriptionChanged(it)) },
                onDriverTabSelected = { viewModel.onEvent(RidesEvent.DriverTabChanged(it)) },
                onStartNewDraft = { viewModel.onEvent(RidesEvent.StartNewDriverDraft) },
                onEditDraft = { draftId -> viewModel.onEvent(RidesEvent.EditDriverDraft(draftId)) },
                onDeleteDraft = { draftId -> viewModel.onEvent(RidesEvent.DeleteDriverDraft(draftId)) },
                onMyRideSelected = { rideId ->
                    navController.navigate(Destinations.ActiveRideManagement.createRoute(rideId))
                },
                onOpenPendingSync = {
                    navController.navigate(Destinations.PendingRideSync.route)
                },
                onDismissPublishRideInfo = {
                    viewModel.onEvent(RidesEvent.DismissPublishRideInfo)
                },
                onSeeRide = {
                    viewModel.onEvent(RidesEvent.DriverTabChanged(DriverRidesTab.MY_RIDES))
                    viewModel.onEvent(RidesEvent.DismissPublishRideInfo)
                },
                onPublishRide = {
                    viewModel.onEvent(RidesEvent.PublishRide)
                }
            )

            state.rideActionInfo?.let { infoNotice ->
                RideActionInfoDialog(
                    notice = infoNotice,
                    onDismiss = {
                        viewModel.onEvent(RidesEvent.DismissRideActionInfo)
                    }
                )
            }
        }
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
                onClearRating = { viewModel.onEvent(RidesEvent.ClearRatingFilter) },
                onClearFilters = { viewModel.onEvent(RidesEvent.ClearPassengerFilters) }
            )
        }

        item {
            if (!state.isCreateRideOnline && state.allRides.isNotEmpty()) {
                OfflineAvailableRidesBanner()
            }
        }

        item {
            if (state.nearbyRides.isLoading || state.nearbyRides.isActive || state.nearbyRides.errorMessage != null) {
                NearbyRidesBanner(
                    state = state,
                    onClear = { viewModel.onEvent(RidesEvent.ClearNearbyRides) }
                )
            }
        }

        item {
            state.smartSuggestion?.let { suggestion ->
                SmartSuggestionCard(
                    message = suggestion.message,
                    onClick = { viewModel.onEvent(RidesEvent.ApplySuggestedDestination) }
                )
            }
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

        when {
            state.isLoading -> {
                item {
                    Text(
                        text = "Loading available rides...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            state.filteredRides.isEmpty() -> {
                item {
                    Text(
                        text = "No published rides match these filters right now.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                items(state.filteredRides, key = { it.id }) { ride ->
                    RideCard(
                        ride = ride,
                        onRequest = {
                            navController.navigate(Destinations.RideRequest.createRoute(ride.id))
                        },
                        onOpenReviews = {
                            navController.navigate(
                                Destinations.ReviewsRatings.createRoute(
                                    origin = Destinations.Rides.route,
                                    driverId = ride.driverId,
                                    driverName = ride.driver
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverCreateRideScreen(
    state: RidesUiState,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onOriginChanged: (String) -> Unit,
    onOriginFieldFocused: () -> Unit,
    onOriginSuggestionSelected: (LocationSuggestion) -> Unit,
    onUseCurrentOriginClicked: () -> Unit,
    onDestinationChanged: (String) -> Unit,
    onDestinationFieldFocused: () -> Unit,
    onDestinationSuggestionSelected: (LocationSuggestion) -> Unit,
    onUseCurrentDestinationClicked: () -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onIncreaseSeats: () -> Unit,
    onDecreaseSeats: () -> Unit,
    onPriceChanged: (String) -> Unit,
    onCarModelChanged: (String) -> Unit,
    onLicensePlateChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDriverTabSelected: (DriverRidesTab) -> Unit,
    onStartNewDraft: () -> Unit,
    onEditDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    onMyRideSelected: (String) -> Unit,
    onOpenPendingSync: () -> Unit,
    onDismissPublishRideInfo: () -> Unit,
    onSeeRide: () -> Unit,
    onPublishRide: () -> Unit
) {
    val context = LocalContext.current
    var pendingLocationPermissionField by remember { mutableStateOf<RideLocationField?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val field = pendingLocationPermissionField
        pendingLocationPermissionField = null

        if (field == null) return@rememberLauncherForActivityResult

        when {
            granted && field == RideLocationField.ORIGIN -> onUseCurrentOriginClicked()
            granted && field == RideLocationField.DESTINATION -> onUseCurrentDestinationClicked()
            !granted && field == RideLocationField.ORIGIN -> onUseCurrentOriginClicked()
            !granted && field == RideLocationField.DESTINATION -> onUseCurrentDestinationClicked()
        }
    }

    fun requestCurrentLocationFor(field: RideLocationField) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            when (field) {
                RideLocationField.ORIGIN -> onUseCurrentOriginClicked()
                RideLocationField.DESTINATION -> onUseCurrentDestinationClicked()
            }
        } else {
            pendingLocationPermissionField = field
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WheelsBackground)
            .padding(innerPadding)
    ) {
        state.publishRideInfoMessage?.let { infoMessage ->
            PublishRideInfoDialog(
                message = infoMessage,
                onDismiss = onDismissPublishRideInfo,
                onSeeRide = onSeeRide
            )
        }

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
                    DraftAutosaveStatusBanner(
                        isOnline = state.isCreateRideOnline,
                        isSaving = state.isSavingCreateRideDraft,
                        hasActiveDraft = state.activeCreateRideDraftId != null
                    )
                }
                item {
                    EarningsPreviewCard(
                        estimatedEarnings = state.estimatedEarnings,
                        totalSeats = state.totalSeats,
                        pricePerSeat = state.pricePerSeat
                    )
                }

                state.behavioralNudge?.let { nudge ->
                    item {
                        BehavioralNudgeCard(
                            nudge = nudge,
                            averageHoursBeforeCancellation = state.cancellationBehaviorMetrics?.averageHoursBeforeCancellation,
                            cancellationCount = state.cancellationBehaviorMetrics?.cancellationCount
                        )
                    }
                }

                item {
                    FormSection(title = "Route Details") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            LocationAutocompleteField(
                                query = state.origin,
                                selectedSuggestion = state.selectedOrigin,
                                suggestions = state.originSuggestions,
                                currentLocationSuggestion = state.currentLocationSuggestion,
                                showSuggestions = state.showOriginSuggestions,
                                noResults = state.originNoResults,
                                isLoadingCurrentLocation = state.currentLocationLoadingField == RideLocationField.ORIGIN,
                                currentLocationError = state.originLocationError,
                                label = "Pickup Location",
                                placeholder = "e.g., Campus Uniandes - Main Gate",
                                onQueryChanged = onOriginChanged,
                                onSuggestionSelected = onOriginSuggestionSelected,
                                onUseCurrentLocationClicked = { requestCurrentLocationFor(RideLocationField.ORIGIN) },
                                onFieldFocused = onOriginFieldFocused
                            )

                            Box(
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .height(28.dp)
                                    .width(0.dp)
                                    .border(1.dp, Border, RoundedCornerShape(2.dp))
                            )

                            LocationAutocompleteField(
                                query = state.destination,
                                selectedSuggestion = state.selectedDestination,
                                suggestions = state.destinationSuggestions,
                                currentLocationSuggestion = state.currentLocationSuggestion,
                                showSuggestions = state.showDestinationSuggestions,
                                noResults = state.destinationNoResults,
                                isLoadingCurrentLocation = state.currentLocationLoadingField == RideLocationField.DESTINATION,
                                currentLocationError = state.destinationLocationError,
                                label = "Destination",
                                placeholder = "e.g., Centro Comercial Andino",
                                onQueryChanged = onDestinationChanged,
                                onSuggestionSelected = onDestinationSuggestionSelected,
                                onUseCurrentLocationClicked = { requestCurrentLocationFor(RideLocationField.DESTINATION) },
                                onFieldFocused = onDestinationFieldFocused
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
                                maxLength = 10,
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
                                maxLength = 5,
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    val selectedDateIsToday = state.date == String.format(
                                        "%04d-%02d-%02d",
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH) + 1,
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    val initialHour = if (selectedDateIsToday) {
                                        calendar.get(Calendar.HOUR_OF_DAY)
                                    } else {
                                        calendar.get(Calendar.HOUR_OF_DAY)
                                    }
                                    val initialMinute = if (selectedDateIsToday) {
                                        calendar.get(Calendar.MINUTE)
                                    } else {
                                        calendar.get(Calendar.MINUTE)
                                    }
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            onTimeChanged(String.format("%02d:%02d", hourOfDay, minute))
                                        },
                                        initialHour,
                                        initialMinute,
                                        true
                                    ).show()
                                }
                            )
                            state.scheduleValidationMessage?.let { validationMessage ->
                                Text(
                                    text = validationMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFDC2626)
                                )
                            }
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
                                    keyboardType = KeyboardType.Number,
                                    maxLength = 6
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
                                leadingIcon = Icons.Default.DirectionsCar,
                                maxLength = 60
                            )
                            FormTextField(
                                value = state.licensePlate,
                                onValueChange = onLicensePlateChanged,
                                label = "License Plate",
                                placeholder = "ABC-123",
                                leadingIcon = Icons.Default.VerifiedUser,
                                maxLength = 10
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            supportingText = {
                                Text(
                                    text = "${state.description.length}/180",
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
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
            } else if (state.driverSelectedTab == DriverRidesTab.DRAFTS) {
                when {
                    state.isLoadingCreateRideDrafts -> {
                        item {
                            DraftsSummaryCard(
                                draftCount = state.createRideDrafts.size,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        item {
                            DraftsOfflineBanner()
                        }
                        item {
                            Text(
                                text = "Loading your saved drafts...",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    state.createRideDrafts.isEmpty() -> {
                        item {
                            DraftsSummaryCard(
                                draftCount = 0,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        item {
                            Button(
                                onClick = onStartNewDraft,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start New Draft")
                            }
                        }
                        item {
                            DraftsOfflineBanner()
                        }
                        item {
                            EmptyDriverDraftsState(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }

                    else -> {
                        item {
                            DraftsSummaryCard(
                                draftCount = state.createRideDrafts.size,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        item {
                            Button(
                                onClick = onStartNewDraft,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start New Draft")
                            }
                        }
                        item {
                            DraftsOfflineBanner()
                        }
                        items(state.createRideDrafts, key = { it.draftId }) { draft ->
                            DriverDraftCard(
                                draft = draft,
                                isDeleting = state.deletingCreateRideDraftId == draft.draftId,
                                isActive = state.activeCreateRideDraftId == draft.draftId,
                                onEditDraft = { onEditDraft(draft.draftId) },
                                onDeleteDraft = { onDeleteDraft(draft.draftId) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    MyRidesSummary(
                        rideCount = state.driverRides.size,
                        currentTrustScore = state.currentDriverTrustScore,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onOpenPendingSync,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pending Sync")
                    }
                }

                if (state.isShowingCachedDriverRides && state.driverRides.isNotEmpty()) {
                    item {
                        OfflineDriverRidesBanner()
                    }
                }

                when {
                    state.isLoadingDriverRides -> {
                        item {
                            Text(
                                text = "Loading your rides...",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    state.driverRides.isEmpty() -> {
                        item {
                            EmptyDriverRidesState(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                    else -> {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!state.isCreateRideOnline) {
                        Text(
                            text = "You are offline. If you publish now, we will save this ride locally and publish it automatically when your connection returns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onPublishRide,
                        enabled = state.canPublishRide,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        content = {
                            Text(
                                text = if (state.isPublishingRide) "Publishing..." else "Publish Ride",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    )
                    state.publishRideErrorMessage?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PublishRideInfoDialog(
    message: String,
    onDismiss: () -> Unit,
    onSeeRide: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onSeeRide) {
                Text("See ride")
            }
        },
        title = {
            Text(
                text = "Ride update",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
private fun DraftAutosaveStatusBanner(
    isOnline: Boolean,
    isSaving: Boolean,
    hasActiveDraft: Boolean
) {
    if (isOnline && !hasActiveDraft && !isSaving) return

    val message = when {
        !isOnline && isSaving -> "You are offline. We are saving this draft locally on your device."
        !isOnline -> "You are offline. Draft changes stay stored locally and will still be available in Drafts."
        isSaving -> "Saving your draft locally..."
        else -> "This ride is being autosaved locally while you edit it."
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFFE8F0F9) else Color(0xFFFEF3C7)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Drafts,
                contentDescription = null,
                tint = if (isOnline) SecondaryBlue else Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun DraftsSummaryCard(
    draftCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0F9))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Saved Drafts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$draftCount local drafts available for editing or deletion.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DraftsOfflineBanner() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Drafts are stored locally with Room, so you can keep editing without internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun EmptyDriverDraftsState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "No local drafts yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start a ride in Create Ride and we will autosave it here while you work.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DriverDraftCard(
    draft: CreateRideDraftSummary,
    isDeleting: Boolean,
    isActive: Boolean,
    onEditDraft: () -> Unit,
    onDeleteDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (draft.origin.isNotBlank()) draft.origin else "Unnamed draft",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFE8F0F9)
                    ) {
                        Text(
                            text = "Open",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (draft.destination.isNotBlank()) draft.destination else "Destination pending",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Last updated ${formatDraftTimestamp(draft.updatedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${draft.totalSeats} seats • ${draft.pricePerSeat.ifBlank { "--" }} per seat",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onEditDraft,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Edit Draft")
                }
                OutlinedButton(
                    onClick = onDeleteDraft,
                    enabled = !isDeleting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete")
                }
            }
        }
    }
}

@Composable
private fun RideActionInfoDialog(
    notice: RideActionInfoNotice,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (notice.previousTrustScore != null && notice.newTrustScore != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WheelsBackground
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Previous trust score",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${notice.previousTrustScore}%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current trust score",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${notice.newTrustScore}%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BehavioralNudgeCard(
    nudge: BehavioralNudge,
    averageHoursBeforeCancellation: Double?,
    cancellationCount: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nudge.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = nudge.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (averageHoursBeforeCancellation != null &&
                averageHoursBeforeCancellation >= 0 &&
                cancellationCount != null
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recent pattern: $cancellationCount cancellations, average ${String.format("%.1f", averageHoursBeforeCancellation)} hours before departure.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun OfflineAvailableRidesBanner() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "You are offline. We are showing the last available rides we cached from Firebase.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
private fun OfflineDriverRidesBanner() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFF59E0B)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "You are offline. We are showing the last My Rides list we cached from Firebase.",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue
            )
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
            text = "Drafts",
            selected = selectedTab == DriverRidesTab.DRAFTS,
            onClick = { onTabSelected(DriverRidesTab.DRAFTS) },
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
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MyRidesSummary(
    rideCount: Int,
    currentTrustScore: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElectricGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Your Driver Rides",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WheelsSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$rideCount rides ordered by date and departure time",
                style = MaterialTheme.typography.bodyMedium,
                color = WheelsSurface.copy(alpha = 0.9f)
            )
            if (currentTrustScore != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Current trust score: $currentTrustScore%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = WheelsSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyDriverRidesState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WheelsSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "No driver rides yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create a ride and it will appear here. Offline rides will stay as pending to publish until your connection returns.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

private fun formatDraftTimestamp(timestampMillis: Long): String {
    return runCatching {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestampMillis))
    }.getOrDefault("recently")
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
                    DriverRideStatusChip(
                        status = ride.status,
                        pendingSyncAction = ride.pendingSyncAction
                    )
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
private fun DriverRideStatusChip(
    status: DriverRideStatus,
    pendingSyncAction: PendingRideActionType? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val (label, backgroundColor, textColor) = if (pendingSyncAction != null) {
        Triple(
            when (pendingSyncAction) {
                PendingRideActionType.START -> "Start Pending"
                PendingRideActionType.COMPLETE -> "End Pending"
                PendingRideActionType.CANCEL -> "Cancel Pending"
                PendingRideActionType.DELETE -> "Delete Pending"
            },
            if (isDark) Color(0xFF3A2E11) else Color(0xFFFEF3C7),
            Color(0xFFF59E0B)
        )
    } else when (status) {
        DriverRideStatus.PUBLISHED -> Triple("Published", if (isDark) Color(0xFF0F2D3A) else Color(0xFFE0F2FE), Color(0xFF0284C7))
        DriverRideStatus.PENDING_TO_PUBLISH -> Triple("Pending to Publish", if (isDark) Color(0xFF3A2E11) else Color(0xFFFEF3C7), Color(0xFFF59E0B))
        DriverRideStatus.ACTIVE -> Triple("Active", if (isDark) Color(0xFF123126) else Color(0xFFD1FAE5), ElectricGreen)
        DriverRideStatus.COMPLETED -> Triple("Completed", if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), if (isDark) Color(0xFFBFDBFE) else PrimaryBlue)
        DriverRideStatus.CANCELLED -> Triple("Cancelled", if (isDark) Color(0xFF3F1D1D) else Color(0xFFFEE2E2), Color(0xFFDC2626))
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
            .background(gradientHeaderBrush())
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
                    tint = GradientHeaderPrimaryContent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back",
                    color = GradientHeaderSecondaryContent,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create a Ride",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GradientHeaderPrimaryContent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Publish your ride and earn money",
            style = MaterialTheme.typography.bodyMedium,
            color = GradientHeaderSecondaryContent
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
    onClick: (() -> Unit)? = null,
    maxLength: Int? = null
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
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                supportingText = {
                    maxLength?.let {
                        Text(
                            text = "${value.length}/$it",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
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
            .background(gradientHeaderBrush())
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
                tint = GradientHeaderPrimaryContent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back",
                color = GradientHeaderSecondaryContent,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Find a Ride",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GradientHeaderPrimaryContent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Available rides from your university",
            style = MaterialTheme.typography.bodyMedium,
            color = GradientHeaderSecondaryContent
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
    onClearRating: () -> Unit,
    onClearFilters: () -> Unit
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
                onClearRating = onClearRating,
                onClearFilters = onClearFilters
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
    onClearRating: () -> Unit,
    onClearFilters: () -> Unit
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

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onClearFilters,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Clear filters")
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
private fun NearbyRidesBanner(
    state: RidesUiState,
    onClear: () -> Unit
) {
    val nearbyState = state.nearbyRides
    val description = when {
        nearbyState.isLoading -> "Finding the rides closest to your current location..."
        nearbyState.errorMessage != null -> nearbyState.errorMessage
        nearbyState.isActive -> {
            val locationLabel = nearbyState.locationName?.takeIf { it.isNotBlank() } ?: "your location"
            if (nearbyState.nearbyRideCount > 0) {
                "Showing rides closest to $locationLabel. ${nearbyState.nearbyRideCount} ride(s) are within 5 km."
            } else {
                "Showing the closest available rides to $locationLabel."
            }
        }
        else -> null
    } ?: return

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (nearbyState.errorMessage == null) Color(0xFFDFF7EA) else Color(0xFFFFF1F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (nearbyState.errorMessage == null) Icons.Default.LocationOn else Icons.Default.Info,
                contentDescription = null,
                tint = if (nearbyState.errorMessage == null) ElectricGreen else Color(0xFFD14343)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            if (!nearbyState.isLoading) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Clear",
                    color = SecondaryBlue,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clickable(onClick = onClear)
                )
            }
        }
    }
}

@Composable
private fun SmartSuggestionCard(
    message: String,
    onClick: () -> Unit
) {
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
                    text = message,
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
            if (ride.isRecommendedByTrustScore) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SecondaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recommended by trust score",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = SecondaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

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

            ride.distanceFromCurrentLocationLabel?.let { distanceLabel ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ElectricGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = distanceLabel,
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
                            text = String.format(java.util.Locale.US, "%.1f", ride.rating),
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
                    tint = if (emphasized) WheelsSurface else SecondaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
