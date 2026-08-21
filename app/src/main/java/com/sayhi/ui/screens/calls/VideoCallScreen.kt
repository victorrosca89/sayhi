package com.sayhi.ui.screens.calls

import androidx.compose.animation.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.ui.theme.*
import org.webrtc.SurfaceViewRenderer
import org.webrtc.RendererCommon

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
    val localSurfaceView by viewModel.localSurfaceView.collectAsState()
    val remoteSurfaceView by viewModel.remoteSurfaceView.collectAsState()

    LaunchedEffect(callerId, callerName) {
        if (isOutgoing) {
            viewModel.initiateCall(callerId, callerName, "video")
        } else {
            viewModel.answerCall(callerId, callerName, "video")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Remote video (full screen)
        remoteSurfaceView?.let { surface ->
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setMirror(false)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    viewModel.attachRemoteSurface(view)
                }
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2D1B4E), Color(0xFF0A0410))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person, null,
                    tint = PurpleLight.copy(alpha = 0.4f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(callerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Local video (PiP)
        localSurfaceView?.let { surface ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 16.dp, top = 80.dp, end = 16.dp, bottom = 120.dp)
                    .size(120.dp, 160.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setMirror(true)
                        }
                    },
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    update = { view ->
                        viewModel.attachLocalSurface(view)
                    }
                )
            }
        }

        // Top overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onEndCall,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(callerName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    when (callState) {
                        "connected" -> callDuration
                        "connecting" -> "Connecting..."
                        "ringing" -> "Ringing"
                        else -> "..."
                    },
                    color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(40.dp))
        }

        // Bottom controls (glassmorphism)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
                .padding(horizontal = 32.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip camera
                VideoCallButton(
                    icon = Icons.Default.FlipCameraAndroid,
                    label = "Flip",
                    onClick = { viewModel.flipCamera() }
                )
                // Mute
                VideoCallButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = { viewModel.toggleMute() }
                )
                // Camera off
                VideoCallButton(
                    icon = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    label = "Camera",
                    isActive = isCameraOff,
                    onClick = { viewModel.toggleCamera() }
                )
                // End call
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = {
                            viewModel.endCall()
                            onEndCall()
                        },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = RedDanger,
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("End", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun VideoCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = if (isActive) RedDanger else Color.White.copy(alpha = 0.15f),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}
