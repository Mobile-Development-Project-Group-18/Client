package com.group18.gosell.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.main.home.HomeScreen
import com.group18.gosell.main.home.HomeViewModel
import com.group18.gosell.main.messages.MessagesScreen
import com.group18.gosell.main.profile.ProfileScreen
import com.group18.gosell.main.profile.ProfileViewModel
import com.group18.gosell.main.sell.SellScreen
import com.group18.gosell.main.sell.SellViewModel
import com.group18.gosell.main.wishlist.WishlistScreen
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GosellTheme

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(mainNavController: NavHostController, authViewModel: AuthViewModel) {
    val bottomNavController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()

    val totalUnreadCount by mainViewModel.totalUnreadCount.collectAsState()
    GosellTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController = bottomNavController, totalUnreadCount = totalUnreadCount) }
        ) { innerPadding ->
            BottomNavGraph(
                bottomNavController = bottomNavController,
                mainNavController = mainNavController,
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel
            )
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController, totalUnreadCount: Int) {
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Messages", Screen.Messages.route, Icons.Filled.MailOutline, Icons.Outlined.MailOutline),
        BottomNavItem("Sell", Screen.Sell.route, Icons.Filled.AddCircleOutline, Icons.Outlined.AddCircleOutline),
        BottomNavItem("Wishlist", Screen.Wishlist.route, Icons.Filled.FavoriteBorder, Icons.Outlined.FavoriteBorder),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = GoSellIconTint
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    if (item.route == Screen.Messages.route) {
                        BadgedBox(badge = {
                            if (totalUnreadCount > 0) {
                                Badge {
                                    Text(
                                        text = totalUnreadCount.toString(),
                                         fontSize = 10.sp,
                                         color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }) {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = GoSellIconTint,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = GoSellIconTint,
                    indicatorColor = MaterialTheme.colorScheme.background
                )
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
        composable(Screen.Messages.route) {
            MessagesScreen(navController = mainNavController)
        }
        composable(Screen.Sell.route) {
            val sellViewModel: SellViewModel = viewModel()
            SellScreen(sellViewModel = sellViewModel, mainNavController = mainNavController)
        }
        composable(Screen.Wishlist.route) {
            WishlistScreen(
                onProductClick = { productId ->
                    mainNavController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                profileViewModel = profileViewModel,
                authViewModel = authViewModel,
                mainNavController = mainNavController
            )
        }
    }
}