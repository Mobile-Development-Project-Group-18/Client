package com.group18.gosell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.auth.LoginScreen
import com.group18.gosell.auth.SignupScreen
import com.group18.gosell.main.MainScreen
import com.group18.gosell.main.detail.ProductDetailScreen // Import Detail Screen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = remember(FirebaseAuth.getInstance().currentUser) { // Remember start destination based on initial state
        if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Main.route
        } else {
            Screen.Login.route
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.Main.route) {
            MainScreen(mainNavController = navController, authViewModel = authViewModel)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(navController = navController, productId = productId)
        }
    }

    LaunchedEffect(authState, navController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        println("Auth State Changed: $authState, Current Route: $currentRoute")

        if (authState == AuthViewModel.AuthenticationState.UNAUTHENTICATED &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.Signup.route) {
            println("Navigating to Login from $currentRoute")
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED &&
            (currentRoute == Screen.Login.route || currentRoute == Screen.Signup.route)) {
            println("Navigating to Main from $currentRoute")
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}