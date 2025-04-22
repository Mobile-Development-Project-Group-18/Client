package com.group18.gosell.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.group18.gosell.main.detail.ProductDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModel: ProductDetailViewModel = viewModel(),
    navController: NavController,
    productId: String?,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var productName by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var place by remember { mutableStateOf(TextFieldValue("")) }
    var type by remember { mutableStateOf(TextFieldValue("")) }
    var image by remember { mutableStateOf(TextFieldValue("")) }
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.product) {
        if (state.product != null && !hasInitialized) {
            state.product?.let { product ->
                productName = TextFieldValue(product.name ?: "")
                description = TextFieldValue(product.description ?: "")
                price = TextFieldValue(product.price.toString())
                place = TextFieldValue(product.place ?: "")
                type = TextFieldValue(product.type ?: "")
                image = TextFieldValue(product.image ?: "")
            }
        }
    }

    LaunchedEffect(productId) {
        productId?.let { viewModel.fetchProductDetails(it) }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("Place") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = image,
                onValueChange = { image = it },
                label = { Text("Image") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val updatedProduct = state.product?.copy(
                        name = productName.text,
                        description = description.text,
                        price = price.text.toDoubleOrNull() ?: 0.0,
                        place = place.text,
                        type = type.text,
                        image = image.text
                    )
                    if (updatedProduct != null) {
                        scope.launch {
                            viewModel.editProduct(updatedProduct)
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("productUpdated", true)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
