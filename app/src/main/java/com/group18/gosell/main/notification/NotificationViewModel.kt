package com.group18.gosell.main.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.group18.gosell.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificationViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var notificationListener: ListenerRegistration? = null

    init {
        observeNotifications()
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                observeNotifications()
            } else {
                clearNotificationObserver()
                _uiState.value = NotificationUiState()
            }
        }
    }

    private fun observeNotifications() {
        clearNotificationObserver()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = NotificationUiState(isLoading = false, error = "User not logged in.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        notificationListener = db.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _uiState.update { it.copy(isLoading = false, error = "Error loading notifications: ${e.localizedMessage}") }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val notifications = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    }
                    val unreadCount = notifications.count { !it.read }
                    _uiState.update {
                        it.copy(
                            notifications = notifications,
                            unreadCount = unreadCount,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                     _uiState.update { it.copy(isLoading = false) }
                }
            }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                db.collection("notifications").document(notificationId)
                    .update("read", true)
                    .await()
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to mark notification as read: ${e.message}") }
                 println("Error marking notification as read: ${e.message}")
            }
        }
    }

     fun markAllNotificationsAsRead() {
         val userId = auth.currentUser?.uid ?: return
         val unreadNotifications = _uiState.value.notifications.filter { !it.read }

         if (unreadNotifications.isEmpty()) return

         viewModelScope.launch {
             try {
                 val batch = db.batch()
                 unreadNotifications.forEach { notification ->
                     val docRef = db.collection("notifications").document(notification.id)
                     batch.update(docRef, "isRead", true)
                 }
                 batch.commit().await()
             } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to mark all notifications as read: ${e.message}") }
                 println("Error marking all notifications as read: ${e.message}")
             }
         }
     }


    private fun clearNotificationObserver() {
        notificationListener?.remove()
        notificationListener = null
    }

    override fun onCleared() {
        super.onCleared()
        clearNotificationObserver()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
