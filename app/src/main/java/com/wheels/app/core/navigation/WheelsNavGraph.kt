package com.wheels.app.core.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wheels.app.core.ui.components.WheelsBottomBar
import com.wheels.app.features.home.presentation.ui.HomeScreen
import com.wheels.app.features.home.presentation.viewmodel.HomeViewModel
import com.wheels.app.features.payments.presentation.ui.PaymentsScreen
import com.wheels.app.features.payments.presentation.ui.QuickPaymentScreen
import com.wheels.app.features.payments.presentation.viewmodel.PaymentsViewModel
import com.wheels.app.features.profile.presentation.ui.ProfileScreen
import com.wheels.app.features.profile.presentation.viewmodel.ProfileViewModel
import com.wheels.app.features.rides.presentation.ui.RidesScreen
import com.wheels.app.features.rides.presentation.viewmodel.RidesViewModel

@Composable
fun WheelsNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedRoute = currentDestination
        ?.hierarchy
        ?.mapNotNull { it.route }
        ?.firstOrNull { route -> wheelsBottomNavItems.any { it.route == route } }
    val isQuickPaymentScreen = currentDestination?.route == Destinations.QuickPayment.route

    Scaffold(
        bottomBar = {
            if (!isQuickPaymentScreen) {
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
            startDestination = Destinations.Home.route
        ) {
            composable(Destinations.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(innerPadding = innerPadding, viewModel = viewModel, navController = navController)
            }
            composable(Destinations.Rides.route) {
                val viewModel: RidesViewModel = hiltViewModel()
                RidesScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.Payments.route) {
                val viewModel: PaymentsViewModel = hiltViewModel()
                PaymentsScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.QuickPayment.route) {
                val viewModel: PaymentsViewModel = hiltViewModel()
                QuickPaymentScreen(innerPadding = innerPadding, navController = navController, viewModel = viewModel)
            }
            composable(Destinations.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
        }
    }
}
