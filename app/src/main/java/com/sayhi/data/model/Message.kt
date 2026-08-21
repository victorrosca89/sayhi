package com.sayhi.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class Message(
    @get:PropertyName("message_id") @set:PropertyName("message_id")
    var messageId: String = "",
    @get:PropertyName("chat_id") @set:PropertyName("chat_id")
    var chatId: String = "",
    @get:PropertyName("sender_id") @set:PropertyName("sender_id")
    var senderId: String = "",
    @get:PropertyName("receiver_id") @set:PropertyName("receiver_id")
    var receiverId: String = "",
    @get:PropertyName("text") @set:PropertyName("text")
    var text: String = "",
    @get:PropertyName("file_name") @set:PropertyName("file_name")
    var fileName: String = "",
    @get:PropertyName("file_size") @set:PropertyName("file_size")
    var fileSize: Long = 0,
    @get:PropertyName("file_type") @set:PropertyName("file_type")
    var fileType: String = "",
    @get:PropertyName("is_pinned") @set:PropertyName("is_pinned")
    var isPinned: Boolean = false,
    @get:PropertyName("is_read") @set:PropertyName("is_read")
    var isRead: Boolean = false,
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    @ServerTimestamp var createdAt: Date? = null
) {
    val isFileMessage: Boolean get() = fileName.isNotEmpty()
}