package com.group18.gosell.main.sell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
import com.group18.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    sellViewModel: SellViewModel,
    mainNavController: NavHostController
) {
    val productName by sellViewModel.productName.collectAsState()
    val description by sellViewModel.description.collectAsState()
    val place by sellViewModel.place.collectAsState()
    val type by sellViewModel.type.collectAsState()
    val price by sellViewModel.price.collectAsState()
    val imageUri by sellViewModel.imageUri.collectAsState()
    val isLoading by sellViewModel.isLoading.collectAsState()
    val error by sellViewModel.error.collectAsState()
    val navigateToDetailId by sellViewModel.navigateToDetailId.collectAsState()

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

    GosellTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Create Listing", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { mainNavController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Photos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    imageUri?.let { uri ->
                        Box(modifier = Modifier.size(100.dp).padding(end = 8.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_launcher_background)
                            )
                            IconButton(
                                onClick = { sellViewModel.clearImage() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear Image", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(GoSellColorSecondary)
                            .clickable(enabled = !isLoading) { sellViewModel.selectImage("content://mock_image_${System.currentTimeMillis()}") } // Use mock URI
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add Photo", tint = GoSellIconTint)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Photo", style = MaterialTheme.typography.labelSmall, color = GoSellTextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                StyledOutlinedTextField(
                    value = productName,
                    onValueChange = { sellViewModel.updateProductName(it) },
                    label = "Title*",
                    isError = error?.contains("name", ignoreCase = true) == true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = price,
                    onValueChange = { sellViewModel.updatePrice(it) },
                    label = "Price*",
                    leadingIcon = { Text("$", color = GoSellTextSecondary) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = error?.contains("price", ignoreCase = true) == true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = description,
                    onValueChange = { sellViewModel.updateDescription(it) },
                    label = "Description",
                    modifier = Modifier.height(120.dp),
                    singleLine = false,
                    maxLines = 5,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = place,
                    onValueChange = { sellViewModel.updatePlace(it) },
                    label = "Location",
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = type,
                    onValueChange = { sellViewModel.updateType(it) },
                    label = "Category",
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { sellViewModel.postProduct() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = productName.isNotBlank() && !isLoading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Post Listing", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GoSellColorSecondary,
            unfocusedContainerColor = GoSellColorSecondary,
            disabledContainerColor = GoSellColorSecondary.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = GoSellTextSecondary,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}