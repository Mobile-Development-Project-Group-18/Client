package com.group18.gosell.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Signup : Screen("signup_screen")
    object Main : Screen("main_screen")
    object Home : Screen("home_screen")
    object Messages : Screen("messages_screen")
    object Sell : Screen("sell_screen")
    object Wishlist : Screen("wishlist_screen")
    object Profile : Screen("profile_screen")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object UserListings : Screen("user_listings_screen")

    object ChatDetail : Screen("chat_detail/{chatId}/{otherUserId}") {
        fun createRoute(chatId: String, otherUserId: String) = "chat_detail/$chatId/$otherUserId"
    }
}