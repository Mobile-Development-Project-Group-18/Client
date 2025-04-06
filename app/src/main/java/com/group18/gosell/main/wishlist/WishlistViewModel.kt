package com.group18.gosell.main.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WishlistState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WishlistViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(WishlistState(isLoading = true))
    val uiState: StateFlow<WishlistState> = _uiState

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            _uiState.value = WishlistState(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val userDoc = db.collection("users").document(userId).get().await()
                val user = userDoc.toObject(User::class.java)
                
                val wishlistProducts = mutableListOf<Product>()
                user?.wishlist?.forEach { productId ->
                    try {
                        val productDoc = db.collection("products").document(productId).get().await()
                        productDoc.toObject(Product::class.java)?.let { product ->
                            wishlistProducts.add(product.copy(id = productDoc.id))
                        }
                    } catch (e: Exception) {
                        println("Failed to load product $productId: ${e.message}")
                    }
                }
                
                _uiState.value = WishlistState(products = wishlistProducts)
            } catch (e: Exception) {
                _uiState.value = WishlistState(error = e.localizedMessage)
            }
        }
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val userRef = db.collection("users").document(userId)
                
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentUser = snapshot.toObject(User::class.java)
                    val updatedWishlist = currentUser?.wishlist?.filter { it != productId } ?: emptyList()
                    transaction.update(userRef, "wishlist", updatedWishlist)
                }.await()

                loadWishlist()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
