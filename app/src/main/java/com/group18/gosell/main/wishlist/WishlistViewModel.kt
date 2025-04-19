 package com.group18.gosell.main.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
import com.group18.gosell.data.model.User
import com.group18.gosell.data.model.WishList
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

    private val _wishlistItems = MutableStateFlow<List<WishList>>(emptyList())
    val wishlistItems: StateFlow<List<WishList>> = _wishlistItems
    
    private val _uiState = MutableStateFlow(WishlistState(isLoading = true))
    val uiState: StateFlow<WishlistState> = _uiState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            _uiState.value = WishlistState(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

                val wishlist = api.getUserWishList(userId)

                _wishlistItems.value = wishlist

                val wishlistProducts = wishlist.mapNotNull { item ->
                    try {
                        val response = api.getProductById(item.productId)
                        if (response.isSuccessful) {
                            response.body()
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        println("Failed to load product ${item.productId}: ${e.message}")
                        null
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
                val currentWishList = _wishlistItems.value
                val existingFavorite = currentWishList.find { it.productId == productId }

                if (existingFavorite?.favoriteId != null) {
                    val response = api.removeWishList(existingFavorite.favoriteId)
                    if (response.isSuccessful) {

                        _wishlistItems.value = currentWishList.filterNot { it.productId == productId }

                        val updatedProducts = _uiState.value.products.filterNot { it.id == productId }
                        _uiState.value = _uiState.value.copy(products = updatedProducts)
                    } else {
                        _uiState.value = _uiState.value.copy(error = "Failed to remove from wishlist")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(error = "WishList not found")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage)
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
                        val updatedProducts = _uiState.value.products.filterNot { it.id == productId }
                        _uiState.value = _uiState.value.copy(products = updatedProducts)
                    } else {
                        _error.value = "Failed to remove favorite"
                    }
                } else {
                    // Add
                    val newWish = WishList(userId = currentUser.uid, productId = productId)
                    val response = api.addWishList(newWish)
                    if (response.isSuccessful) {
                        loadWishlist() // refresh to get correct IDs from backend
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
        _uiState.value = _uiState.value.copy(error = null)
    }
}
