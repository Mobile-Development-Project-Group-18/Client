package com.group18.gosell.main.sell

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GoSellColorSecondary
import com.group18.gosell.ui.theme.GoSellIconTint
import com.group18.gosell.ui.theme.GoSellTextSecondary
import com.group18.gosell.ui.theme.GosellTheme
import com.group18.gosell.R
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    sellViewModel: SellViewModel,
    mainNavController: NavHostController
) {
    val productName by sellViewModel.productName.collectAsState()
    val description by sellViewModel.description.collectAsState()
    val place by sellViewModel.place.collectAsState()
    val categories by sellViewModel.categories.collectAsState()
    val imageUris by sellViewModel.imageUris.collectAsState()
    val isLoading by sellViewModel.isLoading.collectAsState()
    val error by sellViewModel.error.collectAsState()
    val navigateToDetailId by sellViewModel.navigateToDetailId.collectAsState()
    val price by sellViewModel.price.collectAsState()
    val addressSuggestions by sellViewModel.addressSuggestions.collectAsState()
    val isLoadingAddresses by sellViewModel.isLoadingAddresses.collectAsState()
    val isGettingLocation by sellViewModel.isGettingLocation.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        sellViewModel.initPlacesApi(context)
    }
    
    val photoFile = remember { File.createTempFile("photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}", ".jpg", context.cacheDir) }
    val photoUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            sellViewModel.addImage(photoUri.toString())
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            sellViewModel.addImage(uri.toString())
        }
    }
    
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            sellViewModel.addImage("content://mock_image_${System.currentTimeMillis()}")
        }
    }
    
    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        sellViewModel.setLocationPermissionGranted(isLocationGranted)
        
        if (isLocationGranted) {
            sellViewModel.useCurrentLocation(context)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission is required to use this feature")
            }
        }
    }

    val availableCategories = listOf(
        "Clothing", "Electronics", "Home", "Books", "Sports", "Toys", 
        "Beauty", "Automotive", "Health", "Garden", "Kitchen", "Furniture", 
        "Baby", "Pets", "Office", "Music", "Art", "Collectibles"
    )
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var tempSelectedCategories by remember { mutableStateOf(categories) }
    
    var customCategoryText by remember { mutableStateOf("") }
    
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

    if (showCategoryDialog) {
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Select Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCategoryText,
                            onValueChange = { customCategoryText = it },
                            label = { Text("Add Custom Category") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = { 
                                if (customCategoryText.isNotBlank() && !tempSelectedCategories.contains(customCategoryText.trim())) {
                                    tempSelectedCategories = tempSelectedCategories + customCategoryText.trim()
                                    customCategoryText = "" 
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Custom Category")
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(availableCategories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSelectedCategories = if (tempSelectedCategories.contains(category)) {
                                            tempSelectedCategories - category
                                        } else {
                                            tempSelectedCategories + category
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tempSelectedCategories.contains(category),
                                    onCheckedChange = { checked ->
                                        tempSelectedCategories = if (checked) {
                                            tempSelectedCategories + category
                                        } else {
                                            tempSelectedCategories - category
                                        }
                                    }
                                )
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    
                    if (tempSelectedCategories.isNotEmpty()) {
                        Text(
                            "Selected: ${tempSelectedCategories.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            items(tempSelectedCategories) { category ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { showCategoryDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = { 
                                sellViewModel.updateCategories(tempSelectedCategories)
                                showCategoryDialog = false 
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    GosellTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Create Listing", fontWeight = FontWeight.Bold) },
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
                Text("Photos", 
                    style = MaterialTheme.typography.titleMedium, 
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )
                
                // Images horizontal list
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add photo button
                    item {
                        var showImageOptions by remember { mutableStateOf(false) }
                        
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(GoSellColorSecondary)
                                .clickable(enabled = !isLoading) { showImageOptions = true }
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add Photo", tint = GoSellIconTint)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add Photo", style = MaterialTheme.typography.labelSmall, color = GoSellTextSecondary)
                            }
                        }
                        
                        // Image options dialog
                        if (showImageOptions) {
                            AlertDialog(
                                onDismissRequest = { showImageOptions = false },
                                title = { Text("Add Photo") },
                                text = { Text("Choose an option") },
                                confirmButton = {
                                    Column {
                                        Button(
                                            onClick = {
                                                showImageOptions = false
                                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Take Photo")
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Button(
                                            onClick = {
                                                showImageOptions = false
                                                galleryLauncher.launch("image/*")
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Choose from Gallery")
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showImageOptions = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                    
                    // Show selected images
                    items(imageUris) { uri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_launcher_background)
                            )
                            IconButton(
                                onClick = { sellViewModel.removeImage(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close, 
                                    contentDescription = "Remove Image", 
                                    tint = Color.White, 
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

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

                // Location with suggestions
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        StyledOutlinedTextField(
                            value = place,
                            onValueChange = { sellViewModel.updatePlace(it) },
                            label = "Location",
                            singleLine = true,
                            enabled = !isLoading && !isGettingLocation,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                if (isGettingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(
                                        onClick = { 
                                            // Request location permissions when the user clicks the location button
                                            requestLocationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.MyLocation, 
                                            contentDescription = "Use Current Location"
                                        )
                                    }
                                }
                            }
                        )
                        
                        // Show loading indicator for address suggestions
                        if (isLoadingAddresses) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                        
                        // Address suggestions dropdown
                        if (addressSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    addressSuggestions.forEach { suggestion ->
                                        Text(
                                            text = suggestion,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { sellViewModel.selectAddressSuggestion(suggestion) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Categories section
                Text("Categories", 
                    style = MaterialTheme.typography.titleMedium, 
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )
                
                // Display selected categories as chips
                if (categories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            AssistChip(
                                onClick = { },
                                label = { Text(category) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { sellViewModel.removeCategory(category) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove $category",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Add categories button
                Button(
                    onClick = { 
                        tempSelectedCategories = categories
                        showCategoryDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Category, contentDescription = "Select Categories")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (categories.isEmpty()) "Select Categories" else "Edit Categories")
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { sellViewModel.postProduct(context) },
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
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        shape = MaterialTheme.shapes.small,
        isError = isError,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = {
                onDone?.invoke()
            }
        )
    )
}