package com.group18.gosell.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.main.home.HomeScreen
import com.group18.gosell.main.home.HomeViewModel
import com.group18.gosell.main.ProfileScreen.ProfileScreen
import com.group18.gosell.main.ProfileScreen.ProfileViewModel
import com.group18.gosell.main.sell.SellScreen
import com.group18.gosell.main.sell.SellViewModel
import com.group18.gosell.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavHostController, authViewModel: AuthViewModel) { // Accept mainNavController
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = bottomNavController) }
    ) { innerPadding ->
        BottomNavGraph(
            bottomNavController = bottomNavController, // Controls bottom nav internal navigation
            mainNavController = mainNavController, // Needed to navigate *outside* bottom nav (e.g., to Detail)
            modifier = Modifier.padding(innerPadding),
            authViewModel = authViewModel
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Sell,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val icon = when (screen) {
                Screen.Home -> Icons.Filled.Home
                Screen.Sell -> Icons.Filled.AddCircle
                Screen.Profile -> Icons.Filled.AccountCircle
                else -> Icons.Filled.Home // Should not happen
            }
            val label = when (screen) {
                Screen.Home -> "Home"
                Screen.Sell -> "Sell"
                Screen.Profile -> "Profile"
                else -> ""
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
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
}
@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = bottomNavController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(homeViewModel = homeViewModel, mainNavController = mainNavController)
        }
        composable(Screen.Sell.route) {
            val sellViewModel: SellViewModel = viewModel()
            SellScreen(sellViewModel = sellViewModel, mainNavController = mainNavController)
        }
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(profileViewModel = profileViewModel, authViewModel = authViewModel)
        }
    }
}