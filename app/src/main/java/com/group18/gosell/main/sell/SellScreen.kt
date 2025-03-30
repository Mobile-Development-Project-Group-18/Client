package com.group18.gosell.main.sell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.group18.gosell.navigation.Screen

@Composable
fun SellScreen(
    sellViewModel: SellViewModel,
    mainNavController: NavHostController
) {
    val productName by sellViewModel.productName.collectAsState()
    val description by sellViewModel.description.collectAsState()
    val place by sellViewModel.place.collectAsState()
    val type by sellViewModel.type.collectAsState()
    val imageUri by sellViewModel.imageUri.collectAsState()
    val isLoading by sellViewModel.isLoading.collectAsState()
    val error by sellViewModel.error.collectAsState()
    val navigateToDetailId by sellViewModel.navigateToDetailId.collectAsState() // Observe navigation trigger

    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(navigateToDetailId) {
        navigateToDetailId?.let { productId ->
            mainNavController.navigate(Screen.ProductDetail.createRoute(productId)) {
            }
            sellViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            sellViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sell Your Item", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = productName,
                onValueChange = { sellViewModel.updateProductName(it) },
                label = { Text("Product Name*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error?.contains("name", ignoreCase = true) == true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { sellViewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = place,
                onValueChange = { sellViewModel.updatePlace(it) },
                label = { Text("Place/Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = type,
                onValueChange = { sellViewModel.updateType(it) },
                label = { Text("Type/Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { sellViewModel.selectImage("mock_image_${System.currentTimeMillis()}") },
                    enabled = !isLoading
                ) {
                    Text(if (imageUri == null) "Add Image" else "Change Image")
                }
                if (imageUri != null) {
                    Text("Image Selected!", color = Color.Gray)
                    TextButton(onClick = { sellViewModel.clearImage() }, enabled = !isLoading) {
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { sellViewModel.postProduct() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = productName.isNotBlank() && !isLoading
                ) {
                    Text("Post Product")
                }
            }
        }
    }
}