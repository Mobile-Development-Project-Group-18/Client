package com.group18.gosell.main.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.group18.gosell.data.model.formatPrice
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellRed
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
import com.group18.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.product
    val seller = uiState.seller
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(productId, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!productId.isNullOrBlank()) {
                    viewModel.fetchProductDetails(productId)
                } else {
                    viewModel.clearError()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.navigateToChatId) {
        uiState.navigateToChatId?.let { chatId ->
            val otherUserId = seller?.id ?: "unknown"
            navController.navigate(Screen.ChatDetail.createRoute(chatId, otherUserId))
            viewModel.onChatNavigationComplete()
        }
    }

    GosellTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            product?.name ?: "Product Details",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (product != null) {
                            IconButton(
                                onClick = { viewModel.toggleWishlist() }
                            ) {
                                Icon(
                                    imageVector = if (uiState.isInWishlist) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = if (uiState.isInWishlist) "Remove from Wishlist" else "Add to Wishlist",
                                    tint = if (uiState.isInWishlist) GoSellRed else GoSellIconTint
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                if (product != null && seller != null && seller.id != currentUserId) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.initiateOrGetChat() },
                        icon = {
                            if (uiState.isChatLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Message, "Message Seller")
                            }
                        },
                        text = { Text("Message Seller") },
                        expanded = true,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.error != null && product == null -> {
                        Column(modifier = Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "An unknown error occurred.",
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { if(!productId.isNullOrBlank()) viewModel.fetchProductDetails(productId)}) {
                                Text("Retry")
                            }
                        }
                    }

                    product != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 80.dp)
                        ) {
                            AsyncImage(
                                model = product.image ?: R.drawable.ic_launcher_background,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(GoSellColorSecondary),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_launcher_background)
                            )

                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatPrice(product.price),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = "Location",
                                        tint = GoSellIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = product.place ?: "N/A",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoSellTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = "Posted Time",
                                        tint = GoSellIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatTimestampRelative(product.createdAt),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoSellTextSecondary
                                    )

                                }
                                Spacer(modifier = Modifier.height(20.dp))


                                Text(
                                    "Description",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    product.description ?: "No description provided.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                                if (seller != null && seller.id != currentUserId) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { /* TODO: Navigate to seller profile (optional) */ },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = seller.avatar ?: R.drawable.ic_launcher_background,
                                            contentDescription = "Seller Avatar",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(GoSellColorSecondary),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(id = R.drawable.ic_launcher_background)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${seller.firstName} ${seller.lastName}".trim(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "View Profile",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GoSellTextSecondary
                                            )
                                        }
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = "View Profile",
                                            tint = GoSellIconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                } else if (seller != null && seller.id == currentUserId) {
                                    Text("This is your listing", style = MaterialTheme.typography.bodySmall, color = GoSellTextSecondary, modifier = Modifier.padding(bottom=12.dp))
                                    HorizontalDivider(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                }


                                if (!product.type.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Category",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(product.type, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (uiState.error != null){
                            Text(
                                text = uiState.error?:"",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)).padding(8.dp)
                            )
                        }
                    }

                    else -> {
                        Text(
                            "Product not available.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}


private fun formatTimestampRelative(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
        months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
        weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}