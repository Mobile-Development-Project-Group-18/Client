package com.group18.gosell.main.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.group18.gosell.data.model.Chat
import com.group18.gosell.data.model.Message
import com.group18.gosell.data.model.Notification
import com.group18.gosell.data.model.RetrofitInstance.api
import com.group18.gosell.data.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class SendOfferUiState(
    val offerAmount: String = "",
    val productName: String = "",
    val sellerId: String = "",
    val productId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val offerSent: Boolean = false,
    val navigateToChatId: String? = null,
    val navigateToOtherUserId: String? = null
)

class SendOfferViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(SendOfferUiState())
    val uiState: StateFlow<SendOfferUiState> = _uiState.asStateFlow()

    fun initializeOffer(productId: String, productName: String, sellerId: String, initialOffer: String?) {
         _uiState.update {
            it.copy(
                productId = productId,
                productName = productName,
                sellerId = sellerId,
                offerAmount = initialOffer ?: ""
            )
        }
    }

    fun updateOfferAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*\$"))) {
             _uiState.update { it.copy(offerAmount = amount, error = null) }
        } else {
             _uiState.update { it.copy(error = "Invalid amount") }
        }
    }

    fun sendOffer() {
        val amountString = _uiState.value.offerAmount
        val amount = amountString.toDoubleOrNull()
        val currentUid = auth.currentUser?.uid
        val sellerId = _uiState.value.sellerId
        val productName = _uiState.value.productName

        if (currentUid == null) {
            _uiState.update { it.copy(error = "User not logged in.") }
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid offer amount.") }
            return
        }
        if (sellerId.isBlank() || _uiState.value.productId.isBlank()) {
            _uiState.update { it.copy(error = "Missing seller or product information.") }
            return
        }
        if (currentUid == sellerId) {
            _uiState.update { it.copy(error = "Cannot send offer to yourself.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val chatId = findOrCreateChat(currentUid, sellerId)

                val formattedAmount = String.format("%.2f", amount)
                val offerMessageText = "Offered $$formattedAmount for '$productName'"

                sendMessageInternal(chatId, currentUid, sellerId, offerMessageText)

                _uiState.update { it.copy(
                    isLoading = false,
                    offerSent = true,
                    navigateToChatId = chatId,
                    navigateToOtherUserId = sellerId
                ) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to send offer: ${e.message}") }
            }
        }
    }

    private suspend fun findOrCreateChat(user1Id: String, user2Id: String): String {
        val participants = listOf(user1Id, user2Id).sorted()
        val productId = _uiState.value.productId

        val querySnapshot = db.collection("chats")
            .whereEqualTo("participants", participants)
            .whereEqualTo("productContext", productId)
            .limit(1)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id
        } else {
            coroutineScope {
                val user1Deferred = async { api.getUserById(user1Id) }
                val user2Deferred = async { api.getUserById(user2Id) }

                val user1Response = user1Deferred.await()
                val user2Response = user2Deferred.await()

                val user1 = user1Response.body() ?: User(id = user1Id, firstName = "Unknown", lastName = "User")
                val user2 = user2Response.body() ?: User(id = user2Id, firstName = "Unknown", lastName = "User")

                val newChatId = UUID.randomUUID().toString()
                val newChat = Chat(
                    id = newChatId,
                    participants = participants,
                    participantNames = mapOf(
                        user1Id to "${user1.firstName} ${user1.lastName}".trim(),
                        user2Id to "${user2.firstName} ${user2.lastName}".trim()
                    ),
                    participantAvatars = mapOf(
                        user1Id to user1.avatar,
                        user2Id to user2.avatar
                    ),
                    productContext = productId.takeIf { it.isNotBlank() },
                    productName = _uiState.value.productName.takeIf { it.isNotBlank() },
                    lastMessage = null,
                    lastMessageTimestamp = null,
                    unreadCounts = mapOf(user1Id to 0, user2Id to 0)
                )
                db.collection("chats").document(newChatId).set(newChat).await()
                newChatId
            }
        }
    }

    private suspend fun sendMessageInternal(chatId: String, senderId: String, receiverId: String, text: String) {
        val messageId = db.collection("chats").document(chatId).collection("messages").document().id
        val newMessage = Message(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            timestamp = null,
            isOffer = true
        )

        val senderUser = try {
            api.getUserById(senderId).body()
        } catch (e: Exception) {
            null
        } ?: User(id = senderId, firstName = "Unknown", lastName = "User")
        val senderName = "${senderUser.firstName} ${senderUser.lastName}".trim()

        val notification = Notification(
            receiverId = receiverId,
            senderId = senderId,
            senderName = senderName,
            chatId = chatId,
            messageId = messageId,
            productName = _uiState.value.productName,
            offerText = text,
            timestamp = null,
        )

        val batch = db.batch()

        val messagesRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        batch.set(messagesRef, newMessage)

        val notificationRef = db.collection("notifications").document()
        batch.set(notificationRef, notification)

        val chatRef = db.collection("chats").document(chatId)
        val chatUpdateData = mapOf(
            "lastMessage" to text,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCounts.$receiverId" to FieldValue.increment(1)
        )
        batch.set(chatRef, chatUpdateData, SetOptions.merge())

        batch.commit().await()
    }

    fun resetOfferStatus() {
         _uiState.update { it.copy(offerSent = false, error = null) }
     }

    fun onNavigationComplete() {
        _uiState.update { it.copy(navigateToChatId = null, navigateToOtherUserId = null) }
    }
}