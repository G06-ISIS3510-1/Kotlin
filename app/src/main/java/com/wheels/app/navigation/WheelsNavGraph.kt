package com.wheels.app.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                wheelsBottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (item.badgeCount != null) {
                                BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                                    Text(item.label.first().toString())
                                }
                            } else {
                                Text(item.label.first().toString())
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
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
