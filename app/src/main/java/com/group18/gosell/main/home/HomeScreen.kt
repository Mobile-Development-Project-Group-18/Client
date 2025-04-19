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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import com.group18.gosell.main.wishlist.WishlistViewModel
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellRed
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
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
    wishlistViewModel: WishlistViewModel
) {
    val products by homeViewModel.products.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()

    // --- SEARCH STATE ---
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    
    
    
    

    // --- SORT STATE ---
    var showSortDialog by remember { mutableStateOf(false) }
    var mainSortType by remember { mutableStateOf<String?>(null) }
    var priceSortDirection by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var zipCodeInput by remember { mutableStateOf("") }

    // Helper: Reset sub-options when main sort type changes
    fun resetSubOptions() {
        priceSortDirection = null
        selectedCategory = null
        zipCodeInput = ""
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort/Filter By") },
            text = {
                Column {
                    if (mainSortType == null) {
                        listOf("Price", "Location", "Category").forEach { type ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mainSortType = type
                                        resetSubOptions()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = false,
                                    onClick = {
                                        mainSortType = type
                                        resetSubOptions()
                                    }
                                )
                                Text(type)
                            }
                        }
                    } else when (mainSortType) {
                        "Price" -> {
                            listOf("Low to High", "High to Low").forEach { direction ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            priceSortDirection = direction
                                            showSortDialog = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = priceSortDirection == direction,
                                        onClick = {
                                            priceSortDirection = direction
                                            showSortDialog = false
                                        }
                                    )
                                    Text("Price: $direction")
                                }
                            }
                        }
                        "Location" -> {
                            Column {
                                OutlinedTextField(
                                    value = zipCodeInput,
                                    onValueChange = { value ->
                                        // Only allow up to 5 digits
                                        if (value.length <= 5 && value.all { it.isDigit() }) {
                                            zipCodeInput = value
                                        }
                                    },
                                    label = { Text("Enter Zip Code") },
                                    placeholder = { Text("e.g. 12345") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { showSortDialog = false },
                                    enabled = zipCodeInput.length == 5
                                ) {
                                    Text("Apply")
                                }
                            }
                        }
                        "Category" -> {
                            listOf("Clothing", "Electron", "Home", "Books", "Sports", "Toys").forEach { category ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = category
                                            showSortDialog = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedCategory == category,
                                        onClick = {
                                            selectedCategory = category
                                            showSortDialog = false
                                        }
                                    )
                                    Text(category)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    // Show 'Clear Filter' if a filter is active
                    if (selectedCategory != null || priceSortDirection != null) {
                        TextButton(onClick = {
                            mainSortType = null
                            resetSubOptions()
                        }) {
                            Text("Clear Filter")
                        }
                    } else if (mainSortType != null) {
                        TextButton(onClick = { mainSortType = null }) {
                            Text("Back")
                        }
                    }
                    TextButton(onClick = { showSortDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }
    
    
    
    

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

    GosellTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (isSearching) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search products...") },
                                singleLine = true,
                                maxLines = 1,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isSearching = false
                                    }) {
                                        Icon(Icons.Default.FilterList, contentDescription = "Close Search")
                                    }
                                }
                            )
                        } else {
                            Text("Gosell", fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = !isSearching }) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search Items",
                                tint = GoSellIconTint
                            )
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Filter Items",
                                tint = GoSellIconTint
                            )
                        }
                        IconButton(onClick = { /* TODO: Notifications Action */ }) {
                            BadgedBox(badge = { Badge { Text("2") } }) {
                                Icon(
                                    Icons.Filled.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = GoSellIconTint
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
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
                            // Sort products based on sortOption
                            // Filtering, searching, and sorting logic
val filteredAndSortedProducts = products
    // 1. Search filter
    .filter { product ->
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.trim().lowercase()
            product.name.lowercase().contains(q) || (product.description?.lowercase()?.contains(q) ?: false)
        }
    }
    // 2. Apply sort/filter
    .let { list ->
        when {
            mainSortType == "Category" && selectedCategory != null ->
                list.filter { it.type.equals(selectedCategory, ignoreCase = true) }
            mainSortType == "Price" && priceSortDirection == "Low to High" ->
                list.sortedBy { it.price ?: Double.MAX_VALUE }
            mainSortType == "Price" && priceSortDirection == "High to Low" ->
                list.sortedByDescending { it.price ?: Double.MIN_VALUE }
            mainSortType == "Location" && zipCodeInput.length == 5 ->
                list.filter { it.place == zipCodeInput }
            else -> list
        }
    }

                            items(filteredAndSortedProducts, key = { it.id }) { product ->
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
                                        wishlistViewModel.toggleWishlist(
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