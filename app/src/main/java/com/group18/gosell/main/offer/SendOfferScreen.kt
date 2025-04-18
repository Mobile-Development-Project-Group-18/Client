package com.group18.gosell.main.offer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.group18.gosell.navigation.Screen
import com.group18.gosell.ui.theme.GosellTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendOfferScreen(
    navController: NavController,
    productId: String?,
    productName: String?,
    sellerId: String?,
    initialOffer: String?,
    viewModel: SendOfferViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId, productName, sellerId, initialOffer) {
        if (productId != null && productName != null && sellerId != null) {
            viewModel.initializeOffer(productId, productName, sellerId, initialOffer)
        } else {
            println("Error: Missing data for SendOfferScreen")
        }
    }

    LaunchedEffect(uiState.offerSent) {
         if (uiState.offerSent) {
             navController.popBackStack()
         }
     }

    LaunchedEffect(uiState.navigateToChatId, uiState.navigateToOtherUserId) {
        val chatId = uiState.navigateToChatId
        val otherUserId = uiState.navigateToOtherUserId
        if (chatId != null && otherUserId != null) {
            navController.navigate(Screen.ChatDetail.createRoute(chatId, otherUserId)) {
                popUpTo(Screen.SendOffer.route) { inclusive = true }
            }
            viewModel.onNavigationComplete()
        }
    }

    GosellTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Make an Offer") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                     colors = TopAppBarDefaults.topAppBarColors(
                         containerColor = MaterialTheme.colorScheme.background,
                         titleContentColor = MaterialTheme.colorScheme.onBackground,
                         navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                     )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.productName.isNotBlank()) {
                     Text(
                         text = "You are making an offer for:",
                         style = MaterialTheme.typography.titleMedium
                     )
                     Text(
                         text = uiState.productName,
                         style = MaterialTheme.typography.headlineSmall,
                         textAlign = TextAlign.Center
                     )
                 }

                OutlinedTextField(
                    value = uiState.offerAmount,
                    onValueChange = { viewModel.updateOfferAmount(it) },
                    label = { Text("Your Offer Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                     isError = uiState.error?.contains("amount", ignoreCase = true) == true
                )

                Button(
                    onClick = { viewModel.sendOffer() },
                    enabled = !uiState.isLoading && uiState.offerAmount.isNotBlank() && uiState.error == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Sending Offer...")
                    } else {
                        Text("Send Offer")
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}