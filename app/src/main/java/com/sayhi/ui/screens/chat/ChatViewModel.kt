package com.sayhi.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sayhi.data.model.Message
import com.sayhi.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var currentChatId: String = ""

    fun loadMessages(otherUserId: String) {
        viewModelScope.launch {
            messageRepository.getMessages(getChatId(otherUserId)).collect { msgs ->
                _messages.value = msgs
            }
        }
        viewModelScope.launch {
            messageRepository.markAsRead(getChatId(otherUserId), currentUserId)
        }
    }

    fun sendTextMessage(receiverId: String, text: String) {
        if (text.isBlank() || receiverId.isBlank()) return
        viewModelScope.launch {
            val message = Message(
                chatId = getChatId(receiverId),
                senderId = currentUserId,
                receiverId = receiverId,
                text = text.trim()
            )
            messageRepository.sendMessage(message)
        }
    }

    fun sendFile(receiverId: String, uri: Uri) {
        viewModelScope.launch {
            val fileName = getFileName(uri)
            val fileSize = getFileSize(uri)
            val message = Message(
                chatId = getChatId(receiverId),
                senderId = currentUserId,
                receiverId = receiverId,
                text = "",
                fileName = fileName,
                fileSize = fileSize,
                fileType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            )
            messageRepository.sendMessage(message)
        }
    }

    private fun getChatId(otherUserId: String): String {
        val sorted = listOf(currentUserId, otherUserId).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    private fun getFileName(uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "file"
    }

    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: 0L
    }
}
