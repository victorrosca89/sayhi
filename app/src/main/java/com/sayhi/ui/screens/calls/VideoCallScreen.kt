package com.sayhi.ui.screens.calls

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.ui.theme.*

@Composable
fun VideoCallScreen(
    callerId: String,
    callerName: String,
    isOutgoing: Boolean,
    onEndCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isCameraOff by viewModel.isCameraOff.collectAsState()

    LaunchedEffect(callerId, callerName) {
        if (isOutgoing) viewModel.initiateCall(callerId, callerName, "video")
        else viewModel.answerCall(callerId, callerName, "video")
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.radialGradient(colors = listOf(Color(0xFF2D1B4E), Color(0xFF0A0410)))
    )) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Videocam, null, tint = PurpleLight.copy(alpha = 0.4f), modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(12.dp))
            Text(callerName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                when (callState) { "connected" -> callDuration; "connecting" -> "Connecting..."; "ringing" -> "Ringing"; else -> "..." },
                color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(32.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VideoCallButton(Icons.Default.FlipCameraAndroid, "Flip") { viewModel.flipCamera() }
            VideoCallButton(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, "Mute", isMuted) { viewModel.toggleMute() }
            VideoCallButton(if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam, "Cam", isCameraOff) { viewModel.toggleCamera() }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(onClick = { viewModel.endCall(); onEndCall() }, modifier = Modifier.size(64.dp), shape = CircleShape, color = RedDanger, shadowElevation = 12.dp) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                }
                Spacer(Modifier.height(4.dp))
                Text("End", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun VideoCallButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isActive: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(onClick = onClick, modifier = Modifier.size(52.dp), shape = CircleShape, color = if (isActive) RedDanger else Color.White.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}
