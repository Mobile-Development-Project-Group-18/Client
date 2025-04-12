package com.group18.gosell.main.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
import com.group18.gosell.data.model.User
import com.group18.gosell.data.model.WishList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _wishlistItems = MutableStateFlow<List<WishList>>(emptyList())
    val wishlistItems: StateFlow<List<WishList>> = _wishlistItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchProducts()
        fetchWishlist()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val productList = api.getProducts()
                _products.value = productList

            } catch (e: Exception) {
                _error.value = "Failed to load products: ${e.localizedMessage}"
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun fetchWishlist() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val wishlist = api.getUserWishList(userId)
                _wishlistItems.value = wishlist
            } catch (e: Exception) {
                _error.value = "Failed to load wishlist: ${e.localizedMessage}"
            }
        }
    }

    fun toggleWishlist(productId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val currentWishList = _wishlistItems.value
                val existingFavorite = currentWishList.find { it.productId == productId }

                if (existingFavorite != null) {
                    // Remove
                    val response = api.removeWishList(existingFavorite.favoriteId!!)
                    if (response.isSuccessful) {
                        _wishlistItems.value = currentWishList.filterNot { it.productId == productId }
                    } else {
                        _error.value = "Failed to remove favorite"
                    }
                } else {
                    // Add
                    val newWish = WishList(userId = currentUser.uid, productId = productId)
                    val response = api.addWishList(newWish)
                    if (response.isSuccessful) {
                        fetchWishlist() // refresh to get correct IDs from backend
                    } else {
                        _error.value = "Failed to add favorite"
                    }
                }

            } catch (e: Exception) {
                _error.value = "Failed to update wishlist: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}