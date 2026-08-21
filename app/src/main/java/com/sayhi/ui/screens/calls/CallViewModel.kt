package com.sayhi.ui.screens.calls

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sayhi.service.SignalingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.webrtc.*
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val signalingService: SignalingService,
    private val firestore: FirebaseFirestore
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

    private val _localSurfaceView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val localSurfaceView: StateFlow<SurfaceViewRenderer?> = _localSurfaceView

    private val _remoteSurfaceView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val remoteSurfaceView: StateFlow<SurfaceViewRenderer?> = _remoteSurfaceView

    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var durationJob: Job? = null
    private var callStartTime: Long = 0

    fun initiateCall(targetUserId: String, targetName: String, callType: String) {
        _callState.value = "connecting"
        signalingService.connect()
        signalingService.sendCallSignal(targetUserId, callType) { success ->
            if (success) _callState.value = "ringing"
        }
        setupWebRTC(callType)
    }

    fun answerCall(callerId: String, callerName: String, callType: String) {
        _callState.value = "connecting"
        signalingService.connect()
        setupWebRTC(callType)
        signalingService.sendAnswerSignal(callerId)
    }

    fun endCall() {
        durationJob?.cancel()
        peerConnection?.close()
        signalingService.disconnect()
        _callState.value = "ended"
    }

    fun toggleMute() {
        localAudioTrack?.let {
            val newState = !_isMuted.value
            it.enabled = !newState
            _isMuted.value = newState
        }
    }

    fun toggleSpeaker() {
        _isSpeaker.value = !_isSpeaker.value
    }

    fun toggleCamera() {
        localVideoTrack?.let {
            val newState = !_isCameraOff.value
            it.enabled = !newState
            _isCameraOff.value = newState
        }
    }

    fun flipCamera() {
        // Camera flip logic handled via SignalingService
    }

    fun attachLocalSurface(surface: SurfaceViewRenderer) {
        _localSurfaceView.value = surface
    }

    fun attachRemoteSurface(surface: SurfaceViewRenderer) {
        _remoteSurfaceView.value = surface
    }

    private fun setupWebRTC(callType: String) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val factory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                signalingService.sendIceCandidate(candidate)
            }
            override fun onConnectionChange(state: PeerConnection.ConnectionState) {
                when (state) {
                    PeerConnection.ConnectionState.CONNECTED -> {
                        _callState.value = "connected"
                        startDurationTimer()
                    }
                    PeerConnection.ConnectionState.DISCONNECTED,
                    PeerConnection.ConnectionState.FAILED -> {
                        _callState.value = "ended"
                    }
                    else -> {}
                }
            }
            override fun onAddTrack(track: MediaStreamTrack?) {
                if (track is VideoTrack) {
                    _remoteSurfaceView.value?.let { surface ->
                        track.addSink(surface)
                    }
                }
            }
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
    }

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

    override fun onCleared() {
        super.onCleared()
        endCall()
    }
}
