package com.group18.gosell.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.group18.gosell.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, LOADING, ERROR
    }

    private val _authState = MutableStateFlow(
        if (auth.currentUser != null) AuthenticationState.AUTHENTICATED else AuthenticationState.UNAUTHENTICATED
    )
    val authState: StateFlow<AuthenticationState> = _authState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = if (firebaseAuth.currentUser != null) {
                AuthenticationState.AUTHENTICATED
            } else {
                AuthenticationState.UNAUTHENTICATED
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthenticationState.LOADING
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _error.value = mapFirebaseError(e, "Login failed")
                _authState.value = AuthenticationState.ERROR
                _authState.value = AuthenticationState.UNAUTHENTICATED
            }
        }
    }

    fun signup(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _authState.value = AuthenticationState.LOADING
            _error.value = null
            try {
                if (firstName.isBlank() || lastName.isBlank()) {
                    throw IllegalArgumentException("First and Last name cannot be empty")
                }

                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val newUser = User(
                        id = firebaseUser.uid,
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        email = firebaseUser.email ?: email,
                        createDate = System.currentTimeMillis(),
                        emailVerified = firebaseUser.isEmailVerified

                    )

                    db.collection("users").document(firebaseUser.uid).set(newUser).await()
                } else {
                    throw Exception("Firebase user creation failed unexpectedly.")
                }

            } catch (e: Exception) {
                _error.value = mapFirebaseError(e, "Signup failed")
                _authState.value = AuthenticationState.ERROR
                if (auth.currentUser == null) {
                    _authState.value = AuthenticationState.UNAUTHENTICATED
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearError() {
        _error.value = null
    }

    private fun mapFirebaseError(e: Exception, defaultMessage: String): String {
        return when (e) {
            is FirebaseAuthUserCollisionException -> "Email address already in use."
            is FirebaseAuthWeakPasswordException -> "Password is too weak (at least 6 characters)."
            is IllegalArgumentException -> e.message ?: defaultMessage
            else -> e.localizedMessage ?: defaultMessage
        }
    }
}