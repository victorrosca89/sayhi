package com.sayhi.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class ChatInfo(
    @get:PropertyName("chat_id") @set:PropertyName("chat_id")
    var chatId: String = "",
    @get:PropertyName("participants") @set:PropertyName("participants")
    var participants: List<String> = emptyList(),
    @get:PropertyName("last_message") @set:PropertyName("last_message")
    var lastMessage: String = "",
    @get:PropertyName("last_message_time") @set:PropertyName("last_message_time")
    @ServerTimestamp var lastMessageTime: Date? = null,
    @get:PropertyName("unread_count") @set:PropertyName("unread_count")
    var unreadCount: Int = 0,
    @get:PropertyName("is_pinned") @set:PropertyName("is_pinned")
    var isPinned: Boolean = false,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at")
    @ServerTimestamp var updatedAt: Date? = null
)