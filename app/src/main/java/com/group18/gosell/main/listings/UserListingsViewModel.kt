package com.group18.gosell.main.listings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserListingsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _listings = MutableStateFlow<List<Product>>(emptyList())
    val listings: StateFlow<List<Product>> = _listings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchUserListings()
    }

    fun fetchUserListings() {
        val userId = auth.currentUser?.uid
        Log.d("ProductAPI", "User ID: $userId")
        if (userId == null) {
            _error.value = "User not logged in."
            _listings.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getProductByUserId(userId.toString())
                _listings.value = response

            } catch (e: Exception) {
                _error.value = "Failed to load your listings: ${e.localizedMessage}"
                _listings.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}