package com.sayhi.data.repository

import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query.Direction
import com.sayhi.data.model.ChatInfo
import com.sayhi.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getChats(userId: String): Flow<List<ChatInfo>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val chats = snapshot?.documents?.mapNotNull { it.toObject(ChatInfo::class.java) }
                    ?.sortedByDescending { it.updatedAt } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("created_at", Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val msgs = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) }
                    ?: emptyList()
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(message: Message): Result<Message> = runCatching {
        val chatId = getOrCreateChatId(message.senderId, message.receiverId)
        val msg = message.copy(chatId = chatId, messageId = "${chatId}_${System.currentTimeMillis()}")
        firestore.collection("chats").document(chatId)
            .collection("messages").document(msg.messageId).set(msg).await()
        firestore.collection("chats").document(chatId).set(
            mapOf(
                "participants" to listOf(message.senderId, message.receiverId),
                "last_message" to msg.text.ifEmpty { "📎 ${msg.fileName}" },
                "updated_at" to FieldValue.serverTimestamp()
            ), SetOptions.merge()
        ).await()
        msg
    }

    suspend fun markAsRead(chatId: String, userId: String) {
        val batch = firestore.batch()
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .whereEqualTo("receiver_id", userId)
            .whereEqualTo("is_read", false)
            .get().await().documents.forEach { doc ->
                batch.update(doc.reference, "is_read", true)
            }
        batch.commit().await()
    }

    suspend fun togglePin(chatId: String, isPinned: Boolean) {
        firestore.collection("chats").document(chatId)
            .update("is_pinned", isPinned).await()
    }

    private suspend fun getOrCreateChatId(senderId: String, receiverId: String): String {
        val sorted = listOf(senderId, receiverId).sorted()
        val potentialId = "${sorted[0]}_${sorted[1]}"
        val doc = firestore.collection("chats").document(potentialId).get().await()
        return if (doc.exists()) potentialId else potentialId
    }
}