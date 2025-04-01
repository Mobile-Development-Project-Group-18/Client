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
                
                // Check if product is in user's wishlist
                val currentUser = auth.currentUser
                val isInWishlist = if (currentUser != null) {
                    val userDoc = db.collection("users").document(currentUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)
                    user?.wishlist?.contains(productId) ?: false
                } else {
                    false
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
        val currentUser = auth.currentUser ?: return
        val product = _uiState.value.product ?: return

        viewModelScope.launch {
            try {
                val userRef = db.collection("users").document(currentUser.uid)
                val userDoc = userRef.get().await()
                val user = userDoc.toObject(User::class.java)
                val currentWishlist = user?.wishlist?.toMutableList() ?: mutableListOf()

                if (_uiState.value.isInWishlist) {
                    currentWishlist.remove(product.id)
                } else {
                    if (!currentWishlist.contains(product.id)) {
                        currentWishlist.add(product.id)
                    }
                }

                userRef.update("wishlist", currentWishlist).await()
                _uiState.value = _uiState.value.copy(isInWishlist = !_uiState.value.isInWishlist)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update wishlist: ${e.message}")
            }
        }
    }
}