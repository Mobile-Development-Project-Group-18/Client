package com.group18.gosell.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        auth.addAuthStateListener {
            if (it.currentUser != null) {
                fetchUserProfile()
            } else {
                _user.value = null
            }
        }
        if(auth.currentUser != null) {
            fetchUserProfile()
        }
    }

    private fun fetchUserProfile() {
        val currentAuthUser = auth.currentUser
        if (currentAuthUser == null) {
            _error.value = "No user logged in."
            _user.value = null
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val docRef = db.collection("users").document(currentAuthUser.uid)
                val documentSnapshot = docRef.get().await()

                if (documentSnapshot.exists()) {
                    val userProfile = documentSnapshot.toObject(User::class.java)
                    _user.value = userProfile?.copy(id = documentSnapshot.id)

                    if (userProfile != null && userProfile.emailVerified != currentAuthUser.isEmailVerified) {
                        _user.value = _user.value?.copy(emailVerified = currentAuthUser.isEmailVerified)
                    }

                } else {
                    _error.value = "User profile data not found. Please complete your profile."
                    _user.value = User(id = currentAuthUser.uid, email = currentAuthUser.email ?: "", firstName = "User", lastName = "") // Placeholder
                }

            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.localizedMessage}"
                _user.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}