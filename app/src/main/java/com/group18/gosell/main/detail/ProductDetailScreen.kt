package com.group18.gosell.main.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.group18.gosell.data.model.formatPrice
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(productId) {
        if (!productId.isNullOrBlank()) {
            viewModel.fetchProductDetails(productId)
        } else {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Standard back navigation
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "An unknown error occurred.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                product != null -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("IMAGE PLACEHOLDER", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
                        }

                        Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatPrice(product.price),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Description", style = MaterialTheme.typography.titleMedium)
                        Text(product.description ?: "No description provided.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Category/Type", style = MaterialTheme.typography.titleMedium)
                        Text(product.type ?: "N/A", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Location", style = MaterialTheme.typography.titleMedium)
                        Text(product.place ?: "N/A", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Seller Info if available
                        if (seller != null) {
                            Text("Seller", style = MaterialTheme.typography.titleMedium)
                            Text("${seller.firstName} ${seller.lastName}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (product.sellerId.isNotBlank()){
                            Text("Seller ID: ${product.sellerId}", style = MaterialTheme.typography.bodySmall) // Fallback
                            Spacer(modifier = Modifier.height(8.dp))
                        }


                        Text("Posted on", style = MaterialTheme.typography.titleMedium)
                        Text(formatTimestamp(product.createdAt), style = MaterialTheme.typography.bodySmall)
                        if (product.updatedAt != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Updated on", style = MaterialTheme.typography.titleMedium)
                            Text(formatTimestamp(product.updatedAt), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> {
                    Text("Product data not available.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "N/A"
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}