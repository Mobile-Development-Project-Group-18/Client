package com.group18.gosell.main.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        if (userId == null) {
            _error.value = "User not logged in."
            _listings.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = db.collection("products")
                    .whereEqualTo("sellerId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val productList = result.documents.mapNotNull { document ->
                    document.toObject(Product::class.java)?.copy(id = document.id)
                }
                _listings.value = productList

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