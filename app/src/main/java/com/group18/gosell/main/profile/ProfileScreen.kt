package com.group18.gosell.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.group18.gosell.auth.AuthViewModel
import com.group18.gosell.main.home.ProductItem
import com.group18.gosell.main.listings.UserListingsViewModel
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
import com.group18.gosell.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    mainNavController: NavHostController,
    listingsViewModel: UserListingsViewModel = viewModel()
) {
    val user by profileViewModel.user.collectAsState()
    val isLoadingUser by profileViewModel.isLoading.collectAsState()
    val errorUser by profileViewModel.error.collectAsState()

    val listings by listingsViewModel.listings.collectAsState()
    val isLoadingListings by listingsViewModel.isLoading.collectAsState()
    val errorListings by listingsViewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Listings", "Sold", "Reviews")

    LaunchedEffect(Unit) {
        listingsViewModel.fetchUserListings()
    }

    GosellTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    // Removed the back button since it's redundant with bottom nav
                    actions = {
                        IconButton(onClick = { authViewModel.logout() }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoadingUser) {
                        CircularProgressIndicator(modifier = Modifier.size(80.dp))
                    } else if (user != null) {
                        AsyncImage(
                            model = user?.avatar ?: R.drawable.ic_launcher_background,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(GoSellColorSecondary)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_launcher_background)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4.8", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) // Mock rating
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Member Since", tint = GoSellIconTint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Since ${formatJoinDate(user?.createDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoSellTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { /* TODO: Edit Profile Action */ },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)

                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Edit Profile", fontSize = 13.sp)
                        }

                    } else {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "User Avatar", modifier = Modifier.size(80.dp), tint = GoSellColorSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorUser ?: "Could not load profile", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.5f
                            )
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else GoSellTextSecondary
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = GoSellTextSecondary
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    when (selectedTabIndex) {
                        0 -> {
                            if (isLoadingListings) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else if (errorListings != null) {
                                Text(
                                    errorListings ?: "Error loading listings",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                                )
                            } else if (listings.isEmpty()) {
                                Text(
                                    "No active listings",
                                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                    color = GoSellTextSecondary
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(listings, key = { it.id }) { product ->
                                        ProductItem(
                                            product = product,
                                            onItemClick = { productId ->
                                                mainNavController.navigate(Screen.ProductDetail.createRoute(productId))
                                            },
                                            isInWishlist = false,
                                            onToggleWishlist = {}
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Sold items will appear here", color = GoSellTextSecondary)
                            }
                        }
                        2 -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Reviews will appear here", color = GoSellTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatJoinDate(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return try {
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}