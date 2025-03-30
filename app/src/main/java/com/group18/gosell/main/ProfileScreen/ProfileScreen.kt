package com.group18.gosell.main.ProfileScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group18.gosell.auth.AuthViewModel

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel, authViewModel: AuthViewModel) {
    val user by profileViewModel.user.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(error ?: "Error loading profile", color = MaterialTheme.colorScheme.error)
        } else if (user != null) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${user?.firstName} ${user?.lastName}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.email ?: "No Email",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text("Could not load user profile.")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { authViewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}