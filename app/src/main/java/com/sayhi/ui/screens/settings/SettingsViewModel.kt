package com.sayhi.ui.screens.settings

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.google.firebase.auth.FirebaseAuth
import com.sayhi.data.repository.AuthRepository
import com.sayhi.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val serverUrl = "https://lvr-89-sayhiser.hf.space"

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _notifSound = MutableStateFlow(true)
    val notifSound: StateFlow<Boolean> = _notifSound

    private val _ringtone = MutableStateFlow(true)
    val ringtone: StateFlow<Boolean> = _ringtone

    init {
        viewModelScope.launch {
            dataStore.data.map { prefs ->
                prefs[DARK_MODE_KEY] ?: true
            }.collect { _isDarkMode.value = it }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs ->
                prefs[NOTIF_SOUND_KEY] ?: true
            }.collect { _notifSound.value = it }
        }
        viewModelScope.launch {
            dataStore.data.map { prefs ->
                prefs[RINGTONE_KEY] ?: true
            }.collect { _ringtone.value = it }
        }
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                val user = userRepository.getUserOnce(userId)
                _username.value = user?.username ?: ""
                _isDarkMode.value = user?.darkMode ?: true
                _notifSound.value = user?.notificationSound ?: true
                _ringtone.value = user?.ringtoneSound ?: true
            }
        }
    }

    fun updateUsername(newName: String) {
        if (newName.isBlank() || userId.isEmpty()) return
        viewModelScope.launch {
            userRepository.updateUsername(userId, newName).onSuccess {
                _username.value = newName
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            dataStore.edit { it[DARK_MODE_KEY] = !_isDarkMode.value }
            userRepository.updateSettings(userId, !_isDarkMode.value, _notifSound.value, _ringtone.value)
        }
    }

    fun toggleNotifSound() {
        viewModelScope.launch {
            dataStore.edit { it[NOTIF_SOUND_KEY] = !_notifSound.value }
            userRepository.updateSettings(userId, _isDarkMode.value, !_notifSound.value, _ringtone.value)
        }
    }

    fun toggleRingtone() {
        viewModelScope.launch {
            dataStore.edit { it[RINGTONE_KEY] = !_ringtone.value }
            userRepository.updateSettings(userId, _isDarkMode.value, _notifSound.value, !_ringtone.value)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            if (userId.isNotEmpty()) {
                userRepository.updateOnlineStatus(userId, false)
            }
            authRepository.signOut()
        }
    }

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val NOTIF_SOUND_KEY = booleanPreferencesKey("notif_sound")
        val RINGTONE_KEY = booleanPreferencesKey("ringtone")
    }
}
