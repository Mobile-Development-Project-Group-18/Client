package com.group18.gosell.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.formatPrice
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellRed
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.myapplication.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CategoryItemData(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val products by homeViewModel.products.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val wishlistItems by homeViewModel.wishlistItems.collectAsState()

    val categories = listOf(
        CategoryItemData("Clothing", Icons.Default.Checkroom),
        CategoryItemData("Electron", Icons.Default.PhoneAndroid),
        CategoryItemData("Home", Icons.Default.Home),
        CategoryItemData("Books", Icons.Default.Book),
        CategoryItemData("Sports", Icons.Default.SportsSoccer),
        CategoryItemData("Toys", Icons.Default.Toys)
    )

    LaunchedEffect(Unit) {
        homeViewModel.fetchProducts()
    }


    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(
                                start = 4.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(start = 4.dp, end = 4.dp)
                        ) {
                            items(categories.size) { index ->
                                CategoryItem(category = categories[index])
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Latest Items",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            TextButton(onClick = { /* TODO */ }) {
                                Text("See All")
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                    }
                }

                if (error != null) {
                    item(span = {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(
                            maxLineSpan
                        )
                    }) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error ?: "An unknown error occurred",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { homeViewModel.fetchProducts() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (products.isEmpty() && !isLoading) {
                    item(span = {
                        androidx.compose.foundation.lazy.grid.GridItemSpan(
                            maxLineSpan
                        )
                    }) {
                        Text(
                            "No products found. Sell something!",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onItemClick = { productId ->
                                mainNavController.navigate(
                                    Screen.ProductDetail.createRoute(
                                        productId
                                    )
                                )
                            },
                            isInWishlist = wishlistItems.any { it.productId == product.id },
                            onToggleWishlist = { productId ->
                                homeViewModel.toggleWishlist(
                                    productId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryItemData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { /* TODO: Handle Category Click */ }
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(16.dp),
            color = GoSellColorSecondary,
            contentColor = GoSellIconTint
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(GoSellColorSecondary),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.image ?: R.drawable.ic_launcher_background,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_background)
                )

                IconButton(
                    onClick = { onToggleWishlist(product.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isInWishlist) "Remove from Wishlist" else "Add to Wishlist",
                        tint = if (isInWishlist) GoSellRed else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.place ?: "N/A",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoSellTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = formatTimestampShort(product.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = GoSellTextSecondary
                    )
                }
            }
        }
    }
}

private fun formatTimestampShort(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return try {
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}