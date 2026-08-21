package com.sayhi.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.IceCandidate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalingService @Inject constructor() {

    private var socket: Socket? = null
    private val gson = Gson()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var onOfferReceived: ((String, String) -> Unit)? = null
    var onAnswerReceived: ((String) -> Unit)? = null
    var onIceCandidateReceived: ((IceCandidate) -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null

    fun connect(serverUrl: String = "") {
        if (socket?.connected() == true) return
        val url = serverUrl.ifEmpty { "https://lvr-89-sayhiser.hf.space" }
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 3000
                timeout = 10000
                forceNew = true
            }
            socket = IO.socket(url, opts)
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to signaling server")
                socket?.emit("register", currentUserId)
            }?.on("offer") { args ->
                val data = args[0] as JSONObject
                val sdp = data.getString("sdp")
                val fromId = data.getString("from")
                onOfferReceived?.invoke(sdp, fromId)
            }?.on("answer") { args ->
                val data = args[0] as JSONObject
                val sdp = data.getString("sdp")
                onAnswerReceived?.invoke(sdp)
            }?.on("ice-candidate") { args ->
                val data = args[0] as JSONObject
                val candidate = IceCandidate(
                    data.getString("sdpMid"),
                    data.getInt("sdpMLineIndex"),
                    data.getString("candidate")
                )
                onIceCandidateReceived?.invoke(candidate)
            }?.on("call-ended") { _ ->
                onCallEnded?.invoke()
            }?.on(Socket.EVENT_DISCONNECT) { _ ->
                Log.d(TAG, "Disconnected from signaling server")
            }
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}")
        }
    }

    fun sendCallSignal(targetUserId: String, callType: String, callback: ((Boolean) -> Unit)? = null) {
        socket?.emit("call", JSONObject().apply {
            put("from", currentUserId)
            put("to", targetUserId)
            put("type", callType)
        }, callback)
    }

    fun sendOffer(targetUserId: String, sdp: String) {
        socket?.emit("offer", JSONObject().apply {
            put("from", currentUserId)
            put("to", targetUserId)
            put("sdp", sdp)
        })
    }

    fun sendAnswer(targetUserId: String, sdp: String) {
        socket?.emit("answer", JSONObject().apply {
            put("from", currentUserId)
            put("to", targetUserId)
            put("sdp", sdp)
        })
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        socket?.emit("ice-candidate", JSONObject().apply {
            put("from", currentUserId)
            put("sdpMid", candidate.sdpMid)
            put("sdpMLineIndex", candidate.sdpMLineIndex)
            put("candidate", candidate.sdp)
        })
    }

    fun sendEndCall(targetUserId: String) {
        socket?.emit("end-call", JSONObject().apply {
            put("from", currentUserId)
            put("to", targetUserId)
        })
    }

    fun sendAnswerSignal(callerId: String) {
        // Handled via offer/answer flow
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    companion object {
        private const val TAG = "SignalingService"
    }
}
