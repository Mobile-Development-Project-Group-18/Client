package com.group18.gosell.navigation

import androidx.activity.compose.BackHandler
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.auth.LoginScreen
import com.group18.gosell.auth.SignupScreen
import com.group18.gosell.main.MainScreen
import com.group18.gosell.main.detail.ProductDetailScreen
import com.group18.gosell.main.listings.UserListingsScreen
import com.group18.gosell.main.messages.ChatDetailScreen
import com.group18.gosell.main.notification.NotificationScreen
import com.group18.gosell.main.notification.NotificationViewModel
import com.group18.gosell.main.offer.SendOfferScreen
import com.group18.gosell.main.wishlist.WishlistViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val wishlistViewModel: WishlistViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Keep track of current route for back handling
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val startDestination = remember(authState) {
        when (authState) {
            AuthViewModel.AuthenticationState.AUTHENTICATED -> Screen.Main.route
            AuthViewModel.AuthenticationState.UNAUTHENTICATED -> Screen.Login.route
            else -> Screen.Login.route
        }
    }

    // Handle back press - navigate to Main if not already there
    BackHandler(enabled = currentRoute != Screen.Main.route && currentRoute != Screen.Login.route) {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) { inclusive = false }
            launchSingleTop = true
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
                MainScreen(
                    mainNavController = navController,
                    authViewModel = authViewModel,
                    notificationViewModel = notificationViewModel,
                    wishlistViewModel = wishlistViewModel
                )
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
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                val chatId = backStackEntry.arguments?.getString("chatId")
                val otherUserId = backStackEntry.arguments?.getString("otherUserId")
                ChatDetailScreen(
                    navController = navController,
                    chatId = chatId,
                    otherUserId = if (otherUserId == "null" || otherUserId == "unknown") null else otherUserId
                )
            }
        }

        composable(
            route = Screen.SendOffer.route,
            arguments = Screen.SendOffer.arguments
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString(Screen.SendOffer.ARG_PRODUCT_ID)
            val encodedProductName = backStackEntry.arguments?.getString(Screen.SendOffer.ARG_PRODUCT_NAME)
            val sellerId = backStackEntry.arguments?.getString(Screen.SendOffer.ARG_SELLER_ID)
            val encodedInitialOffer = backStackEntry.arguments?.getString(Screen.SendOffer.ARG_INITIAL_OFFER)

            val productName = encodedProductName?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
            val initialOffer = encodedInitialOffer?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }

            SendOfferScreen(
                navController = navController,
                productId = productId,
                productName = productName,
                sellerId = sellerId,
                initialOffer = initialOffer
            )
        }

        composable(Screen.Notifications.route) {
            if (authState == AuthViewModel.AuthenticationState.AUTHENTICATED) {
                NotificationScreen(
                    navController = navController,
                    viewModel = notificationViewModel
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
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}