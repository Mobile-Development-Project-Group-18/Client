package com.group18.gosell.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Chat(
    var id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String?> = emptyMap(),
    val productContext: String? = null,
    val productName: String? = null,
    val lastMessage: String? = null,
    @ServerTimestamp val lastMessageTimestamp: Date? = null,
    val unreadCounts: Map<String, Int> = emptyMap()
) {
    constructor() : this("", emptyList(), emptyMap(), emptyMap(), null, null, null, null, emptyMap())

    fun otherParticipantId(currentUserId: String): String? {
        return participants.firstOrNull { it != currentUserId }
    }
}

data class Message(
    var id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val isOffer: Boolean = false
) {
    constructor() : this("", "", "", "", null, false)
}

data class Notification(
    var id: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val chatId: String = "",
    val messageId: String = "",
    val productName: String = "",
    val offerText: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    var read: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", "", "", null, false)
}