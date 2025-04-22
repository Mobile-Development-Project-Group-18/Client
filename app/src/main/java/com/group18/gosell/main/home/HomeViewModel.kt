package com.group18.gosell.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchProducts()
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




    fun clearError() {
        _error.value = null
    }
}