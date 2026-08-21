package com.sayhi.ui.screens.messages

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sayhi.data.model.ChatInfo
import com.sayhi.data.model.User
import com.sayhi.data.repository.MessageRepository
import com.sayhi.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var searchQuery by mutableStateOf("")

    private val _chats = MutableStateFlow<List<ChatInfo>>(emptyList())
    val chats: StateFlow<List<ChatInfo>> = _chats

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                messageRepository.getChats(currentUserId).collect { _chats.value = it }
            }
            viewModelScope.launch {
                userRepository.getAllUsersFlow(currentUserId).collect { _users.value = it }
            }
            viewModelScope.launch {
                userRepository.updateOnlineStatus(currentUserId, true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (currentUserId.isNotEmpty()) {
            viewModelScope.launch {
                userRepository.updateOnlineStatus(currentUserId, false)
            }
        }
    }
}