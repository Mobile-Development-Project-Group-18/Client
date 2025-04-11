package com.group18.gosell.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.group18.gosell.data.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    private var chatsListener: ListenerRegistration? = null

    init {
        observeUnreadCount()

        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                observeUnreadCount()
            } else {
                clearUnreadCountObserver()
                _totalUnreadCount.value = 0
            }
        }
    }

    private fun observeUnreadCount() {
        clearUnreadCountObserver()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _totalUnreadCount.value = 0
            return
        }

        chatsListener = db.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("MainViewModel: Error listening for chat updates: ${e.localizedMessage}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val chats = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)
                    }
                    val totalUnread = chats.sumOf { chat ->
                        chat.unreadCounts[userId] ?: 0
                    }
                    _totalUnreadCount.value = totalUnread
                }
            }
    }

    private fun clearUnreadCountObserver() {
        chatsListener?.remove()
        chatsListener = null
    }

    override fun onCleared() {
        super.onCleared()
        clearUnreadCountObserver()
    }
}