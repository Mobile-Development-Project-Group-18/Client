package com.group18.gosell.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProductDetailState(
    val product: Product? = null,
    val seller: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInWishlist: Boolean = false
)

class ProductDetailViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProductDetailState())
    val uiState: StateFlow<ProductDetailState> = _uiState

    fun fetchProductDetails(productId: String) {
        if (productId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Invalid Product ID.", isLoading = false)
            return
        }
        if (_uiState.value.isLoading || _uiState.value.product?.id == productId) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ProductDetailState(isLoading = true)
            try {
                val productDoc = db.collection("products").document(productId).get().await()
                val product = productDoc.toObject(Product::class.java)?.copy(id = productDoc.id)

                if (product == null) {
                    _uiState.value = ProductDetailState(error = "Product not found.", isLoading = false)
                    return@launch
                }

                var seller: User? = null
                if (product.sellerId.isNotBlank()) {
                    try {
                        val sellerDoc = db.collection("users").document(product.sellerId).get().await()
                        seller = sellerDoc.toObject(User::class.java)?.copy(id = sellerDoc.id)
                    } catch (sellerError: Exception) {
                        println("Warning: Could not fetch seller info for product $productId: ${sellerError.message}")
                    }
                }

                // Check if product is in user's wishlist
                var isInWishlist = false
                auth.currentUser?.let { user ->
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val currentUser = userDoc.toObject(User::class.java)
                    isInWishlist = currentUser?.wishlist?.contains(productId) == true
                }

                _uiState.value = ProductDetailState(
                    product = product,
                    seller = seller,
                    isLoading = false,
                    error = null,
                    isInWishlist = isInWishlist
                )

            } catch (e: Exception) {
                _uiState.value = ProductDetailState(
                    error = "Failed to load product details: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleWishlist() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val productId = _uiState.value.product?.id ?: throw Exception("Product not found")
                val userRef = db.collection("users").document(userId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentUser = snapshot.toObject(User::class.java)
                    val currentWishlist = currentUser?.wishlist ?: emptyList()
                    
                    val updatedWishlist = if (currentWishlist.contains(productId)) {
                        currentWishlist.filter { it != productId }
                    } else {
                        currentWishlist + productId
                    }
                    
                    transaction.update(userRef, "wishlist", updatedWishlist)
                }.await()

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isInWishlist = !_uiState.value.isInWishlist
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage)
            }
        }
    }
}