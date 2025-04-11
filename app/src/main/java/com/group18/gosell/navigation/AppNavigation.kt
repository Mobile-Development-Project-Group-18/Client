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
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.auth.LoginScreen
import com.group18.gosell.auth.SignupScreen
import com.group18.gosell.main.MainScreen
import com.group18.gosell.main.detail.ProductDetailScreen
import com.group18.gosell.main.listings.UserListingsScreen
import com.group18.gosell.main.messages.ChatDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = remember(authState) {
        when (authState) {
            AuthViewModel.AuthenticationState.AUTHENTICATED -> Screen.Main.route
            AuthViewModel.AuthenticationState.UNAUTHENTICATED -> Screen.Login.route
            else -> Screen.Login.route
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            if (authState != AuthViewModel.AuthenticationState.AUTHENTICATED) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
        }
        composable(Screen.Signup.route) {
            if (authState != AuthViewModel.AuthenticationState.AUTHENTICATED) {
                SignupScreen(navController = navController, authViewModel = authViewModel)
            }
        }
        composable(Screen.Main.route) {
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                MainScreen(mainNavController = navController, authViewModel = authViewModel)
            }
        }
        composable(Screen.UserListings.route) {
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                UserListingsScreen(mainNavController = navController)
            }
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                val productId = backStackEntry.arguments?.getString("productId")
                ProductDetailScreen(navController = navController, productId = productId)
            }
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType } // Keep as string, handle "null" string if needed
            )
        ) { backStackEntry ->
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                val chatId = backStackEntry.arguments?.getString("chatId")
                val otherUserId = backStackEntry.arguments?.getString("otherUserId")
                ChatDetailScreen(
                    navController = navController,
                    chatId = chatId,
                    otherUserId = if (otherUserId == "null" || otherUserId == "unknown") null else otherUserId // Handle the "null" string case
                )
            }
        }
    }

    LaunchedEffect(authState, navController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (authState == AuthViewModel.AuthenticationState.UNAUTHENTICATED &&
            currentRoute != Screen.Login.route && currentRoute != Screen.Signup.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED &&
            (currentRoute == Screen.Login.route || currentRoute == Screen.Signup.route)) {
            navController.navigate(Screen.Main.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true } // Pop everything including Login/Signup
                launchSingleTop = true
            }
        }
    }
}