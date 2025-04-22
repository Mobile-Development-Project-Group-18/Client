package com.group18.gosell.main.sell

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.group18.gosell.BuildConfig
import com.group18.gosell.data.model.Product
import com.group18.gosell.data.model.RetrofitInstance.api
import com.group18.gosell.data.utils.CloudinaryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class SellViewModel : ViewModel() {

    private val auth = Firebase.auth
    private lateinit var placesClient: PlacesClient
    private val sessionToken = AutocompleteSessionToken.newInstance()

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description
    private val _place = MutableStateFlow("")
    val place: StateFlow<String> = _place
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories
    private val _categoryText = MutableStateFlow("")
    private val _imageUris = MutableStateFlow<List<String>>(emptyList())
    val imageUris: StateFlow<List<String>> = _imageUris
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _navigateToDetailId = MutableStateFlow<String?>(null)
    val navigateToDetailId: StateFlow<String?> = _navigateToDetailId
    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price
    private val _addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    val addressSuggestions: StateFlow<List<String>> = _addressSuggestions
    private val _isLoadingAddresses = MutableStateFlow(false)
    val isLoadingAddresses: StateFlow<Boolean> = _isLoadingAddresses
    private val _isGettingLocation = MutableStateFlow(false)
    val isGettingLocation: StateFlow<Boolean> = _isGettingLocation
    private val _locationPermissionGranted = MutableStateFlow(false)
    private val _placePredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())

    fun updateProductName(name: String) {
        _productName.value = name
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun initPlacesApi(context: Context) {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.PLACES_API_KEY)
        }
        placesClient = Places.createClient(context)
    }

    fun updatePlace(loc: String) {
        _place.value = loc
        fetchAddressSuggestions(loc)
    }

    private fun fetchAddressSuggestions(query: String) {
        if (query.length < 3) {
            _addressSuggestions.value = emptyList()
            _placePredictions.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoadingAddresses.value = true
            try {
                if (::placesClient.isInitialized) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setTypesFilter(listOf(PlaceTypes.ADDRESS))
                        .setQuery(query)
                        .setSessionToken(sessionToken)
                        .build()

                    val response = withContext(Dispatchers.IO) {
                        try {
                            placesClient.findAutocompletePredictions(request).await()
                        } catch (e: ApiException) {
                            Log.e("SellViewModel", "Place API error: ${e.statusCode}")
                            null
                        }
                    }

                    if (response != null) {
                        _placePredictions.value = response.autocompletePredictions
                        _addressSuggestions.value = response.autocompletePredictions.map { it.getFullText(null).toString() }
                    } else {
                        _addressSuggestions.value = emptyList()
                        _placePredictions.value = emptyList()
                        Log.w("SellViewModel", "Places API request failed or returned null.")
                    }
                } else {
                    Log.e("SellViewModel", "PlacesClient not initialized.")
                    _addressSuggestions.value = emptyList()
                    _placePredictions.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("SellViewModel", "Error fetching suggestions: ${e.message}")
                _addressSuggestions.value = emptyList()
                _placePredictions.value = emptyList()
            } finally {
                _isLoadingAddresses.value = false
            }
        }
    }

    fun selectAddressSuggestion(address: String) {
        _place.value = address
        _addressSuggestions.value = emptyList()

        val selectedPrediction = _placePredictions.value.find {
            it.getFullText(null).toString() == address
        }

        selectedPrediction?.let {
            Log.d("SellViewModel", "Selected place ID: ${it.placeId}")
        }

        _placePredictions.value = emptyList()
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    fun useCurrentLocation(context: Context) {
        if (!_locationPermissionGranted.value) {
            _error.value = "Location permission is required."
            return
        }

        viewModelScope.launch {
            _isGettingLocation.value = true
            try {
                val location = getCurrentLocation(context)
                val address = getAddressFromLocation(context, location.latitude, location.longitude)
                _place.value = address ?: "Location: ${location.latitude}, ${location.longitude}"
                _addressSuggestions.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to get current location: ${e.localizedMessage}"
                Log.e("SellViewModel", "Error using current location", e)
            } finally {
                _isGettingLocation.value = false
            }
        }
    }

    @Throws(SecurityException::class, Exception::class)
    private suspend fun getCurrentLocation(context: Context): Location = withContext(Dispatchers.IO) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Location permission not granted")
        }

        val cancellationToken = object : CancellationToken() {
            override fun onCanceledRequested(listener: OnTokenCanceledListener): CancellationToken { return this }
            override fun isCancellationRequested(): Boolean { return false }
        }

        try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken).await()
            if (location != null) return@withContext location

            Log.w("SellViewModel", "High accuracy location null, trying last known location.")
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) return@withContext lastLocation

            Log.e("SellViewModel", "Both current and last location are null.")
            throw Exception("Unable to get location")
        } catch (e: SecurityException) {
            Log.e("SellViewModel", "Location permission error in getCurrentLocation", e)
            throw e
        } catch (e: Exception) {
            Log.e("SellViewModel", "Error getting location", e)
            throw Exception("Error getting location: ${e.message}")
        }
    }

    private suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = try {
                     geocoder.getFromLocation(latitude, longitude, 1)
                } catch (ioe: IOException) {
                    Log.e("SellViewModel", "Geocoder service not available", ioe)
                    null
                } catch (iae: IllegalArgumentException) {
                    Log.e("SellViewModel", "Invalid lat/lon for Geocoder", iae)
                    null
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressParts = listOfNotNull(
                        address.thoroughfare, address.locality, address.adminArea,
                        address.postalCode, address.countryName
                    )
                    return@withContext addressParts.joinToString(", ").takeIf { it.isNotBlank() }
                }
                Log.w("SellViewModel", "Geocoder returned no addresses.")
                return@withContext null
            } catch (e: Exception) {
                Log.e("SellViewModel", "Error during geocoding", e)
                return@withContext null
            }
        }

    fun updateCategories(newCategories: List<String>) {
        _categories.value = newCategories
    }

    fun removeCategory(category: String) {
        _categories.value = _categories.value.filter { it != category }
    }

    fun updatePrice(priceStr: String) {
        if (priceStr.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _price.value = priceStr
        } else if (priceStr.isEmpty()){
            _price.value = ""
        }
    }

    fun addImage(uri: String) {
        if (!_imageUris.value.contains(uri)) {
            _imageUris.value += uri
        }
    }

    fun removeImage(uri: String) {
        _imageUris.value = _imageUris.value.filter { it != uri }
    }

    fun postProduct(context: Context) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "User not logged in."
            return
        }
        if (_productName.value.isBlank()) {
            _error.value = "Product name cannot be empty."
            return
        }
        val priceDouble = _price.value.toDoubleOrNull()
        if (_price.value.isNotEmpty() && priceDouble == null) {
            _error.value = "Invalid price format."
            return
        }
        if (priceDouble != null && priceDouble < 0) {
            _error.value = "Price cannot be negative."
            return
        }
        if (_place.value.isBlank()) {
            _error.value = "Location cannot be empty."
             return
        }
         if (_categories.value.isEmpty()) {
             _error.value = "Please select at least one category."
             return
         }
         if (_imageUris.value.isEmpty()) {
             _error.value = "Please add at least one image."
             return
         }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _navigateToDetailId.value = null

            try {
                val categoryString = _categories.value.joinToString(", ").takeIf { it.isNotEmpty() } // Corrected syntax
                var cloudinaryUrl: String? = null

                if (_imageUris.value.isNotEmpty()) {
                    val firstImageUriString = _imageUris.value.first()
                    val uri = try { Uri.parse(firstImageUriString) } catch (e: Exception) { null }

                    if (uri != null) {
                        Log.d("SellViewModel", "Uploading image URI: $uri")
                        cloudinaryUrl = withContext(Dispatchers.IO) {
                            CloudinaryManager.uploadImage(uri, context)
                        }
                        Log.d("SellViewModel", "Cloudinary URL: $cloudinaryUrl")
                        if (cloudinaryUrl == null) {
                             throw IOException("Image upload failed.")
                        }
                    } else {
                         throw IllegalArgumentException("Invalid image URI.")
                    }
                }

                val newProduct = Product(
                    name = _productName.value.trim(),
                    description = _description.value.trim().takeIf { it.isNotBlank() },
                    place = _place.value.trim().takeIf { it.isNotBlank() },
                    sellerId = userId,
                    image = cloudinaryUrl,
                    type = categoryString,
                    price = priceDouble,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                )

                Log.d("SellViewModel", "Posting product: $newProduct")
                val response = api.addProduct(newProduct)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("SellViewModel", "Product posted successfully with ID: ${response.body()!!}")
                    _navigateToDetailId.value = response.body()!!
                    resetForm()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown API error"
                    Log.e("SellViewModel", "API error posting product: ${response.code()} - $errorBody")
                    _error.value = "Failed to post product: $errorBody"
                }

            } catch (e: Exception) {
                Log.e("SellViewModel", "Error posting product", e)
                _error.value = "Failed to post product: ${e.localizedMessage}"
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
        _categories.value = emptyList()
        _categoryText.value = ""
        _price.value = ""
        _imageUris.value = emptyList()
        _addressSuggestions.value = emptyList()
        _placePredictions.value = emptyList()
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
        return withContext(Dispatchers.IO) {
            val latch = java.util.concurrent.CountDownLatch(1)
            var result: T? = null
            var exception: Exception? = null

            this@await.addOnSuccessListener {
                result = it
                latch.countDown()
            }.addOnFailureListener {
                exception = it
                latch.countDown()
            }

            latch.await()

            if (exception != null) {
                throw exception!!
            } else if (result != null) {
                 result!!
            } else {
                throw IllegalStateException("Task completed without result or exception.")
            }
        }
    }
}