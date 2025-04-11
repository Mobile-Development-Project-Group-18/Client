package com.group18.gosell.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Chat(
    var id: String = "",
    val participants: List<String> = emptyList(), // List of User IDs
    val participantNames: Map<String, String> = emptyMap(), // userId -> "FirstName LastName"
    val participantAvatars: Map<String, String?> = emptyMap(), // userId -> avatarUrl
    val productContext: String? = null, // Optional: Product ID this chat is about
    val lastMessage: String? = null,
    @ServerTimestamp val lastMessageTimestamp: Date? = null,
    val unreadCounts: Map<String, Int> = emptyMap() // userId -> count of unread messages
) {
    constructor() : this("", emptyList(), emptyMap(), emptyMap(), null, null, null, emptyMap())

    fun otherParticipantId(currentUserId: String): String? {
        return participants.firstOrNull { it != currentUserId }
    }
}

// Represents a single message within a chat
data class Message(
    var id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    // isRead status can be managed by comparing message timestamp with last read timestamp,
    // or using the unreadCounts in the Chat object. We'll use unreadCounts here.
) {
    constructor() : this("", "", "", "", null)
}