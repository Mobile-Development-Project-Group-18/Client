package com.group18.gosell.main.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.group18.gosell.data.model.Chat
import com.group18.gosell.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val chatInfo: Chat? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val messageToSend: String = ""
)

class ChatDetailViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private var chatId: String? = null
    private var otherUserId: String? = null

    fun initializeChat(chatId: String, otherUserId: String?) {
        if (this.chatId == chatId) return
        this.chatId = chatId
        this.otherUserId = otherUserId
        loadMessages(chatId)
        loadChatInfo(chatId)
        markMessagesAsRead(chatId)
    }

    fun updateMessageToSend(text: String) {
        _uiState.value = _uiState.value.copy(messageToSend = text)
    }


    private fun loadMessages(chatId: String) {
        viewModelScope.launch {
            db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error loading messages: ${error.localizedMessage}"
                        )
                        return@addSnapshotListener
                    }

                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
        }
    }

    private fun loadChatInfo(chatId: String) {
        viewModelScope.launch {
            try {
                val chatDoc = db.collection("chats").document(chatId).get().await()
                val chat = chatDoc.toObject(Chat::class.java)?.copy(id = chatDoc.id)
                _uiState.value = _uiState.value.copy(chatInfo = chat)
            } catch (e: Exception) {
                println("Error fetching chat info: ${e.message}")
            }
        }
    }


    fun sendMessage() {
        val currentUid = auth.currentUser?.uid ?: return
        val currentChatId = chatId ?: return
        val text = _uiState.value.messageToSend.trim()

        if (text.isBlank()) return

        val newMessage = Message(
            chatId = currentChatId,
            senderId = currentUid,
            text = text,
            timestamp = null
        )

        viewModelScope.launch {
            try {
                val batch = db.batch()

                val messagesRef = db.collection("chats").document(currentChatId).collection("messages").document()
                batch.set(messagesRef, newMessage)

                val chatRef = db.collection("chats").document(currentChatId)
                val chatUpdateData = hashMapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp()
                )
                if (otherUserId != null) {
                    chatUpdateData["unreadCounts.$otherUserId"] = FieldValue.increment(1)
                }

                batch.update(chatRef, chatUpdateData)

                batch.commit().await()

                _uiState.value = _uiState.value.copy(messageToSend = "")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to send message: ${e.localizedMessage}")
            }
        }
    }

    private fun markMessagesAsRead(chatId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val chatRef = db.collection("chats").document(chatId)
                chatRef.update("unreadCounts.$currentUid", 0).await()
            } catch (e: Exception) {
                println("Error marking messages as read for chat $chatId: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}