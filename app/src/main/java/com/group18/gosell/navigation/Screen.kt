package com.group18.gosell.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
    object Notifications : Screen("notifications")

    object ChatDetail : Screen("chat_detail/{chatId}/{otherUserId}") {
        fun createRoute(chatId: String, otherUserId: String) = "chat_detail/$chatId/$otherUserId"
    }

    object SendOffer : Screen(
        route = "send_offer/{productId}/{productName}/{sellerId}?initialOffer={initialOffer}"
    ) {
        const val ARG_PRODUCT_ID = "productId"
        const val ARG_PRODUCT_NAME = "productName"
        const val ARG_SELLER_ID = "sellerId"
        const val ARG_INITIAL_OFFER = "initialOffer"

        fun createRoute(productId: String, productName: String, sellerId: String, initialOffer: String? = null): String {
            val encodedProductName = URLEncoder.encode(productName, StandardCharsets.UTF_8.toString())
            val baseRoute = "send_offer/$productId/$encodedProductName/$sellerId"
            return if (initialOffer != null) {
                val encodedOffer = URLEncoder.encode(initialOffer, StandardCharsets.UTF_8.toString())
                "$baseRoute?initialOffer=$encodedOffer"
            } else {
                baseRoute
            }
        }

        val arguments = listOf(
            navArgument(ARG_PRODUCT_ID) { type = NavType.StringType },
            navArgument(ARG_PRODUCT_NAME) { type = NavType.StringType },
            navArgument(ARG_SELLER_ID) { type = NavType.StringType },
            navArgument(ARG_INITIAL_OFFER) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    }
}