package com.sayhi.ui.screens.calls

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.ui.theme.*

@Composable
fun VoiceCallScreen(
    callerId: String,
    callerName: String,
    isOutgoing: Boolean,
    onEndCall: () -> Unit,
    onSwitchToVideo: (String, String) -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeaker by viewModel.isSpeaker.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    LaunchedEffect(callerId, callerName) {
        if (isOutgoing) {
            viewModel.initiateCall(callerId, callerName, "voice")
        } else {
            viewModel.answerCall(callerId, callerName, "voice")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2D1B4E),
                        Color(0xFF0F051D)
                    ),
                    center = androidx.compose.ui.geometry.Offset(0.5f, 0.3f)
                )
            )
    ) {
        // Ambient glow behind avatar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .size(200.dp)
                .background(
                    PurpleDeep.copy(alpha = glowAlpha),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Caller name
            Text(
                callerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Avatar with ring and glow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(20.dp, CircleShape, ambientColor = PurpleDeep.copy(alpha = 0.4f), spotColor = PurpleDeep.copy(alpha = 0.4f))
                    .border(4.dp, Color(0xFF2D1B4E), CircleShape)
                    .clip(CircleShape)
                    .background(PurpleLight.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, null,
                    tint = PurpleLight.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status
            Text(
                when (callState) {
                    "connecting" -> "Calling..."
                    "ringing" -> "Ringing"
                    "connected" -> callDuration
                    else -> "Connecting..."
                },
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker
                CallActionButton(
                    icon = if (isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = "Speaker",
                    isActive = isSpeaker,
                    onClick = { viewModel.toggleSpeaker() }
                )

                // Video
                CallActionButton(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    isActive = false,
                    onClick = { onSwitchToVideo(callerId, callerName) }
                )

                // Mute
                CallActionButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = { viewModel.toggleMute() }
                )

                // End call
                CallEndButton(onClick = {
                    viewModel.endCall()
                    onEndCall()
                })
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = if (isActive) PurpleVivid else DarkSurface,
            shadowElevation = if (isActive) 8.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, null,
                    tint = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun CallEndButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "endPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "endScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                },
            shape = CircleShape,
            color = RedDanger,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CallEnd, null,
                    tint = Color.White, modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("End", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}
