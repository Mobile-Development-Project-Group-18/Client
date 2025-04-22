package com.group18.gosell.main.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group18.gosell.data.model.Chat
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
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
    val isInWishlist: Boolean = false,
    val navigateToChatId: String? = null,
    val isChatLoading: Boolean = false,
    val productDeleted: Boolean = false
)

class ProductDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProductDetailState())
    val uiState: StateFlow<ProductDetailState> = _uiState

    fun fetchProductDetails(productId: String) {
        if (productId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Invalid Product ID.", isLoading = false)
            return
        }
        if (_uiState.value.isLoading || (_uiState.value.product?.id == productId && _uiState.value.seller != null)) {
            _uiState.value = _uiState.value.copy(navigateToChatId = null)
            return
        }

        viewModelScope.launch {
            _uiState.value = ProductDetailState(isLoading = true)
            try {
                val response = api.getProductById(productId)
                if (!response.isSuccessful || response.body() == null) {
                    _uiState.value = ProductDetailState(error = "Product not found.", isLoading = false)
                    return@launch
                }

                val product = response.body()!!

                val currentUser = auth.currentUser
                var currentUserData: User? = null

                val isInWishlist = if (currentUser != null) {
                    try {
                        val wishlist = api.getUserWishList(currentUser.uid)
                        wishlist.any { it.productId == productId }
                    } catch (e: Exception) {
                        println("Failed to fetch wishlist: ${e.message}")
                        false
                    }
                } else {
                    false
                }

                //Get seller
                var seller: User? = null
                if (product.sellerId.isNotBlank() && product.sellerId != currentUser?.uid) {
                    try {
                        val response = api.getUserById(product.sellerId)
                        if (response.isSuccessful) {
                            val user = response.body()
                            seller = user?.copy(id = product.sellerId)
                            Log.d("Seller", "Seller: ${seller}")
                        } else {
                            println("Failed to fetch seller: ${response.code()} ${response.message()}")
                        }
                    } catch (e: Exception) {
                        println("Warning: Could not fetch seller info for product $productId: ${e.message}")
                    }
                } else if (product.sellerId == currentUser?.uid) {
                    try {
                        val response = api.getUserById(product.sellerId)
                        if (response.isSuccessful) {
                            val user = response.body()
                            seller = user?.copy(id = product.sellerId)
                        }
                    } catch (e: Exception) {
                        println("Warning: Could not fetch current user info as seller for product $productId: ${e.message}")
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
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val user = snapshot.toObject(User::class.java)
                    val currentWishlist = user?.wishlist?.toMutableList() ?: mutableListOf()

                    if (_uiState.value.isInWishlist) {
                        currentWishlist.remove(product.id)
                    } else {
                        if (!currentWishlist.contains(product.id)) {
                            currentWishlist.add(product.id)
                        }
                    }
                    transaction.update(userRef, "wishlist", currentWishlist)
                    !_uiState.value.isInWishlist
                }.addOnSuccessListener { isInWishlistAfterTransaction ->
                    _uiState.value = _uiState.value.copy(isInWishlist = isInWishlistAfterTransaction)
                }.addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(error = "Failed to update wishlist: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update wishlist: ${e.message}")
            }
        }
    }

    fun initiateOrGetChat() {
        val currentUser = auth.currentUser ?: return
        val seller = _uiState.value.seller ?: return
        val product = _uiState.value.product ?: return
        val currentUserId = currentUser.uid
        val sellerId = seller.id

        if (currentUserId == sellerId) {
            _uiState.value = _uiState.value.copy(error = "You cannot message yourself.")
            return
        }

        _uiState.value = _uiState.value.copy(isChatLoading = true, error = null)

        viewModelScope.launch {
            try {
                val existingChatQuery = db.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .whereEqualTo("productContext", product.id)
                    .get()
                    .await()

                val specificChat = existingChatQuery.documents.find {
                    val participants = it.get("participants") as? List<*>
                    participants?.contains(sellerId) == true
                }


                if (specificChat != null) {
                    _uiState.value = _uiState.value.copy(navigateToChatId = specificChat.id, isChatLoading = false)
                } else {
                    val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                    val currentUserProfile = currentUserDoc.toObject(User::class.java)
                        ?: throw Exception("Could not retrieve current user profile to create chat.")

                    val newChat = Chat(
                        participants = listOf(currentUserId, sellerId),
                        participantNames = mapOf(
                            currentUserId to "${currentUserProfile.firstName} ${currentUserProfile.lastName}".trim(),
                            sellerId to "${seller.firstName} ${seller.lastName}".trim()
                        ),
                        participantAvatars = mapOf(
                            currentUserId to currentUserProfile.avatar,
                            sellerId to seller.avatar
                        ),
                        productContext = product.id,
                        productName = product.name,
                        lastMessage = null,
                        lastMessageTimestamp = null,
                        unreadCounts = mapOf(currentUserId to 0, sellerId to 0)
                    )

                    val newChatRef = db.collection("chats").add(newChat).await()
                    _uiState.value = _uiState.value.copy(navigateToChatId = newChatRef.id, isChatLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to start chat: ${e.localizedMessage}", isChatLoading = false)
            }
        }
    }

    fun onChatNavigationComplete() {
        _uiState.value = _uiState.value.copy(navigateToChatId = null)
    }

    fun editProduct(updatedProduct: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.updateProduct(updatedProduct.id, updatedProduct)
                if (response.isSuccessful) {
                    Log.d("EditProduct", "success")
                    val product = response.body()
                    _uiState.value = _uiState.value.copy(
                        product = product,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update product: ${response.message()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error updating product: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.deleteProduct(productId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, productDeleted = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete product: ${response.message()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error deleting product: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }


}