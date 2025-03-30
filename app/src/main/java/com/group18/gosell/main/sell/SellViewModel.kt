package com.group18.gosell.main.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SellViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description
    private val _place = MutableStateFlow("")
    val place: StateFlow<String> = _place
    private val _type = MutableStateFlow("")
    val type: StateFlow<String> = _type
    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _navigateToDetailId = MutableStateFlow<String?>(null)
    val navigateToDetailId: StateFlow<String?> = _navigateToDetailId

    fun updateProductName(name: String) {
        _productName.value = name
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun updatePlace(loc: String) {
        _place.value = loc
    }

    fun updateType(t: String) {
        _type.value = t
    }

    fun selectImage(mockUri: String) {
        _imageUri.value = mockUri
    }

    fun clearImage() {
        _imageUri.value = null
    }

    fun postProduct() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in. Please login again."
            return
        }
        if (_productName.value.isBlank()) {
            _error.value = "Product name cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _navigateToDetailId.value = null
            try {
                val newProduct = Product(
                    name = _productName.value.trim(),
                    description = _description.value.trim().takeIf { it.isNotBlank() },
                    place = _place.value.trim().takeIf { it.isNotBlank() },
                    sellerId = userId,
                    image = _imageUri.value,
                    type = _type.value.trim().takeIf { it.isNotBlank() },
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                )

                val documentReference = db.collection("products").add(newProduct).await()

                _navigateToDetailId.value = documentReference.id
                resetForm()

            } catch (e: Exception) {
                _error.value = "Failed to post product: ${e.localizedMessage}"
                _navigateToDetailId.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNavigationComplete() {
        _navigateToDetailId.value = null
    }

    private fun resetForm() {
        _productName.value = ""
        _description.value = ""
        _place.value = ""
        _type.value = ""
        _imageUri.value = null
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}