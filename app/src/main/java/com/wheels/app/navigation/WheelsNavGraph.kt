package com.wheels.app.navigation

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
import com.wheels.app.ui.components.WheelsBottomBar
import com.wheels.app.ui.screens.home.HomeScreen
import com.wheels.app.ui.screens.home.HomeViewModel
import com.wheels.app.ui.screens.payments.PaymentsScreen
import com.wheels.app.ui.screens.payments.PaymentsViewModel
import com.wheels.app.ui.screens.profile.ProfileScreen
import com.wheels.app.ui.screens.profile.ProfileViewModel
import com.wheels.app.ui.screens.rides.RidesScreen
import com.wheels.app.ui.screens.rides.RidesViewModel

@Composable
fun WheelsNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedRoute = currentDestination
        ?.hierarchy
        ?.mapNotNull { it.route }
        ?.firstOrNull { route -> wheelsBottomNavItems.any { it.route == route } }

    Scaffold(
        bottomBar = {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Home.route
        ) {
            composable(Destinations.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.Rides.route) {
                val viewModel: RidesViewModel = hiltViewModel()
                RidesScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.Payments.route) {
                val viewModel: PaymentsViewModel = hiltViewModel()
                PaymentsScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
            composable(Destinations.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(innerPadding = innerPadding, viewModel = viewModel)
            }
        }
    }
}
