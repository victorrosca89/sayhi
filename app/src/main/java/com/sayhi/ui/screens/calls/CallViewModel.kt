package com.sayhi.ui.screens.calls

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sayhi.service.SignalingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val signalingService: SignalingService
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _callState = MutableStateFlow("connecting")
    val callState: StateFlow<String> = _callState
    private val _callDuration = MutableStateFlow("00:00")
    val callDuration: StateFlow<String> = _callDuration
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted
    private val _isSpeaker = MutableStateFlow(false)
    val isSpeaker: StateFlow<Boolean> = _isSpeaker
    private val _isCameraOff = MutableStateFlow(false)
    val isCameraOff: StateFlow<Boolean> = _isCameraOff
    private val _localSurfaceView = MutableStateFlow<Any?>(null)
    val localSurfaceView: StateFlow<Any?> = _localSurfaceView
    private val _remoteSurfaceView = MutableStateFlow<Any?>(null)
    val remoteSurfaceView: StateFlow<Any?> = _remoteSurfaceView
    private var durationJob: Job? = null
    private var callStartTime: Long = 0

    fun initiateCall(targetUserId: String, targetName: String, callType: String) {
        _callState.value = "connecting"
        signalingService.connect()
        signalingService.sendCallSignal(targetUserId, callType) { success ->
            if (success) _callState.value = "ringing"
            else { _callState.value = "connected"; startDurationTimer() }
        }
    }

    fun answerCall(callerId: String, callerName: String, callType: String) {
        _callState.value = "connecting"
        signalingService.connect()
        _callState.value = "connected"
        startDurationTimer()
    }

    fun endCall() {
        durationJob?.cancel()
        signalingService.disconnect()
        _callState.value = "ended"
    }

    fun toggleMute() { _isMuted.value = !_isMuted.value }
    fun toggleSpeaker() { _isSpeaker.value = !_isSpeaker.value }
    fun toggleCamera() { _isCameraOff.value = !_isCameraOff.value }
    fun flipCamera() {}
    fun attachLocalSurface(surface: Any) { _localSurfaceView.value = surface }
    fun attachRemoteSurface(surface: Any) { _remoteSurfaceView.value = surface }

    private fun startDurationTimer() {
        callStartTime = System.currentTimeMillis()
        durationJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - callStartTime) / 1000
                val mins = (elapsed / 60).toString().padStart(2, '0')
                val secs = (elapsed % 60).toString().padStart(2, '0')
                _callDuration.value = "$mins:$secs"
                delay(1000)
            }
        }
    }

    override fun onCleared() { super.onCleared(); endCall() }
}
