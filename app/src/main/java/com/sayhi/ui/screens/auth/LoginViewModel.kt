package com.sayhi.ui.screens.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayhi.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password are required"; return
        }
        viewModelScope.launch {
            _isLoading.value = true; _error.value = null
            authRepository.signInWithEmail(email, password)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Sign in failed" }
            _isLoading.value = false
        }
    }

    fun signUp(username: String, email: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "All fields are required"; return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters"; return
        }
        viewModelScope.launch {
            _isLoading.value = true; _error.value = null
            authRepository.signUpWithEmail(username, email, password)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Sign up failed" }
            _isLoading.value = false
        }
    }

    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true; _error.value = null
            authRepository.signInAnonymously()
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Guest login failed" }
            _isLoading.value = false
        }
    }
}
