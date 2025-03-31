package com.group18.gosell.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _wishlistItems = MutableStateFlow<Set<String>>(emptySet())
    val wishlistItems: StateFlow<Set<String>> = _wishlistItems

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
                val result = db.collection("products")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val productList = result.documents.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.copy(id = document.id)
                }
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
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                _wishlistItems.value = user?.wishlist?.toSet() ?: emptySet()
            } catch (e: Exception) {
                _error.value = "Failed to load wishlist: ${e.localizedMessage}"
            }
        }
    }

    fun toggleWishlist(productId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val userRef = db.collection("users").document(currentUser.uid)
                val userDoc = userRef.get().await()
                val user = userDoc.toObject(User::class.java)
                val currentWishlist = user?.wishlist?.toMutableList() ?: mutableListOf()

                if (currentWishlist.contains(productId)) {
                    currentWishlist.remove(productId)
                } else {
                    currentWishlist.add(productId)
                }

                userRef.update("wishlist", currentWishlist).await()
                _wishlistItems.value = currentWishlist.toSet()
            } catch (e: Exception) {
                _error.value = "Failed to update wishlist: ${e.message}"
            }
        }
    }
    fun clearError() {
        _error.value = null
    }
}