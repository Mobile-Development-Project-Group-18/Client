package com.group18.gosell.main.home

import androidx.compose.foundation.clickable // Import clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController // Import NavController
import com.group18.gosell.data.model.Product
import com.group18.gosell.navigation.Screen // Import Screen

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mainNavController: NavHostController
) {
    val products by homeViewModel.products.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val wishlistItems by homeViewModel.wishlistItems.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.fetchProducts()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = error ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { homeViewModel.fetchProducts() }) {
                    Text("Retry")
                }
            }
        } else if (products.isEmpty()) {
            Text("No products found. Sell something!", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Products for Sale", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(products, key = { it.id }) { product ->
                    ProductItem(
                        product = product,
                        onItemClick = { productId ->
                            mainNavController.navigate(Screen.ProductDetail.createRoute(productId))
                        },
                        isInWishlist = wishlistItems.contains(product.id),
                        onToggleWishlist = { productId -> homeViewModel.toggleWishlist(productId) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onItemClick: (productId: String) -> Unit,
    isInWishlist: Boolean,
    onToggleWishlist: (productId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(product.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("IMG", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onToggleWishlist(product.id) }
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isInWishlist) "Remove from Wishlist" else "Add to Wishlist",
                            tint = if (isInWishlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    product.description ?: "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Type: ${product.type ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                Text("Location: ${product.place ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}