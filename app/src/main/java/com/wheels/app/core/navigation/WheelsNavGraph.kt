package com.wheels.app.core.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wheels.app.core.ui.components.WheelsBottomBar
import com.wheels.app.core.theme.ThemeSettingsViewModel
import com.wheels.app.features.auth.presentation.session.AuthSessionViewModel
import com.wheels.app.features.auth.presentation.session.SessionGateScreen
import com.wheels.app.features.auth.presentation.ui.CreateAccountScreen
import com.wheels.app.features.auth.presentation.ui.ForgotPasswordScreen
import com.wheels.app.features.auth.presentation.ui.SignInScreen
import com.wheels.app.features.auth.presentation.viewmodel.CreateAccountViewModel
import com.wheels.app.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import com.wheels.app.features.auth.presentation.viewmodel.SignInViewModel
import com.wheels.app.features.chat.presentation.ui.GroupChatScreen
import com.wheels.app.features.chat.presentation.viewmodel.GroupChatViewModel
import com.wheels.app.features.home.presentation.ui.HomeScreen
import com.wheels.app.features.home.presentation.viewmodel.HomeViewModel
import com.wheels.app.features.payments.presentation.ui.PaymentsScreen
import com.wheels.app.features.payments.presentation.ui.QuickPaymentScreen
import com.wheels.app.features.payments.presentation.viewmodel.PaymentsViewModel
import com.wheels.app.features.profile.presentation.ui.ProfileScreen
import com.wheels.app.features.profile.presentation.ui.TrustFairnessScreen
import com.wheels.app.features.profile.presentation.ui.UiThemeScreen
import com.wheels.app.features.profile.presentation.viewmodel.ProfileViewModel
import com.wheels.app.features.rides.presentation.ui.ActiveRideManagementScreen
import com.wheels.app.features.rides.presentation.ui.BookingConfirmationScreen
import com.wheels.app.features.rides.presentation.ui.RideRequestScreen
import com.wheels.app.features.rides.presentation.ui.ReviewsRatingsScreen
import com.wheels.app.features.rides.presentation.ui.RidesScreen
import com.wheels.app.features.rides.presentation.viewmodel.RideRequestViewModel
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel

@Composable
fun WheelsNavGraph(themeViewModel: ThemeSettingsViewModel) {
    val navController = rememberNavController()
        val sessionViewModel: AuthSessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val selectedRoute = if (currentDestination?.route == Destinations.ReviewsRatings.route) {
        navBackStackEntry?.arguments?.getString("origin")
    } else {
        currentDestination
            ?.hierarchy
            ?.mapNotNull { it.route }
            ?.firstOrNull { route -> wheelsBottomNavItems.any { it.route == route } }
    }
    val routesWithoutBottomBar = setOf(
        Destinations.SessionGate.route,
        Destinations.SignIn.route,
        Destinations.CreateAccount.route,
        Destinations.ForgotPassword.route,
        Destinations.QuickPayment.route,
        Destinations.GroupChat.route,
        Destinations.RideRequest.route,
        Destinations.ActiveRideManagement.route,
        Destinations.BookingConfirmation.route
    )
    val shouldShowBottomBar = currentDestination?.route !in routesWithoutBottomBar

    LaunchedEffect(sessionState.isLoading, sessionState.authUser, sessionState.isSigningOut, currentRoute) {
        if (sessionState.isLoading) return@LaunchedEffect

        val authRoutes = setOf(
            Destinations.SessionGate.route,
            Destinations.SignIn.route,
            Destinations.CreateAccount.route,
            Destinations.ForgotPassword.route
        )

        if (sessionState.isSigningOut) {
            if (currentRoute != Destinations.SignIn.route) {
                navController.navigate(Destinations.SignIn.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            return@LaunchedEffect
        }

        if (sessionState.authUser != null && currentRoute in authRoutes) {
            navController.navigate(Destinations.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (
            sessionState.authUser == null &&
            (currentRoute == Destinations.SessionGate.route || currentRoute !in authRoutes)
        ) {
            navController.navigate(Destinations.SignIn.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                WheelsBottomBar(
                    items = wheelsBottomNavItems,
                    selectedRoute = selectedRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.SessionGate.route
        ) {
            composable(Destinations.SessionGate.route) {
                SessionGateScreen(innerPadding = innerPadding)
            }
            composable(Destinations.SignIn.route) {
                val viewModel: SignInViewModel = hiltViewModel()
                SignInScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(Destinations.CreateAccount.route) {
                val viewModel: CreateAccountViewModel = hiltViewModel()
                CreateAccountScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(Destinations.ForgotPassword.route) {
                val viewModel: ForgotPasswordViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(Destinations.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(innerPadding = innerPadding, viewModel = viewModel, navController = navController)
            }
            composable(Destinations.Rides.route) {
                val viewModel: RidesViewModel = hiltViewModel()
                RidesScreen(innerPadding = innerPadding, viewModel = viewModel, navController = navController)
            }
            composable(Destinations.Payments.route) {
                val viewModel: PaymentsViewModel = hiltViewModel()
                PaymentsScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.QuickPayment.route) {
                val viewModel: PaymentsViewModel = hiltViewModel()
                QuickPaymentScreen(innerPadding = innerPadding, navController = navController, viewModel = viewModel)
            }
            composable(Destinations.GroupChat.route) {
                val viewModel: GroupChatViewModel = hiltViewModel()
                GroupChatScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(
                route = Destinations.RideRequest.route,
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) {
                val viewModel: RideRequestViewModel = hiltViewModel()
                RideRequestScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(
                route = Destinations.ActiveRideManagement.route,
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) { backStackEntry ->
                val parentEntry = navController.getBackStackEntry(Destinations.Rides.route)
                val viewModel: RidesViewModel = hiltViewModel(parentEntry)
                ActiveRideManagementScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel,
                    rideId = backStackEntry.arguments?.getString("rideId").orEmpty()
                )
            }
            composable(
                route = Destinations.BookingConfirmation.route,
                arguments = listOf(
                    navArgument("rideId") { type = NavType.StringType },
                    navArgument("seats") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val seats = backStackEntry.arguments?.getInt("seats") ?: 1
                val viewModel: RideRequestViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                BookingConfirmationScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    ride = state.ride,
                    selectedSeats = seats,
                    isLoading = state.isLoading
                )
            }
            composable(Destinations.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    innerPadding = innerPadding,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(Destinations.TrustFairness.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                TrustFairnessScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(Destinations.UiTheme.route) {
                UiThemeScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    viewModel = themeViewModel
                )
            }
            composable(
                route = Destinations.ReviewsRatings.route,
                arguments = listOf(
                    navArgument("origin") { type = NavType.StringType },
                    navArgument("driverName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ReviewsRatingsScreen(
                    innerPadding = innerPadding,
                    navController = navController,
                    driverName = backStackEntry.arguments?.getString("driverName").orEmpty()
                )
            }
        }
    }
}
