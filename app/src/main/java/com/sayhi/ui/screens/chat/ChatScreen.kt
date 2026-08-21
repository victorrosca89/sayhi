package com.sayhi.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.data.model.Message
import com.sayhi.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    userId: String,
    username: String,
    avatarUrl: String,
    onBackClick: () -> Unit,
    onVoiceCall: (String, String) -> Unit,
    onVideoCall: (String, String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(userId) {
        viewModel.loadMessages(userId)
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendFile(userId, it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = PurplePrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = PurpleLight.copy(alpha = 0.15f)
                    ) {
                        Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            username, fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Online", fontSize = 13.sp,
                            color = GreenOnline, fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { onVoiceCall(userId, username) }) {
                        Icon(Icons.Default.Call, null, tint = PurplePrimary, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { onVideoCall(userId, username) }) {
                        Icon(Icons.Default.VideoCall, null, tint = PurplePrimary, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { /* menu */ }) {
                        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.messageId }) { message ->
                    MessageBubble(message, viewModel.currentUserId)
                }
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { filePicker.launch("*/*") }) {
                        Icon(
                            Icons.Default.AttachFile, null,
                            tint = TextSecondary, modifier = Modifier.size(24.dp)
                        )
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type message...", color = TextLight) },
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PurplePrimary.copy(alpha = 0.3f),
                            cursorColor = PurplePrimary
                        ),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendTextMessage(userId, messageText)
                                messageText = ""
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = PurplePrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Send, null,
                                    tint = Color.White, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, currentUserId: String) {
    val isMine = message.senderId == currentUserId
    val isDark = !isSystemInDarkTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMine) Align.End else Align.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isMine) 18.dp else 4.dp,
                    topEnd = if (isMine) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (isMine) {
                    PurplePrimary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shadowElevation = 1.dp,
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.isFileMessage) {
                        // File message
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.InsertDriveFile, null,
                                tint = if (isMine) Color.White else PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    message.fileName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                if (message.fileSize > 0) {
                                    Text(
                                        formatFileSize(message.fileSize),
                                        fontSize = 11.sp,
                                        color = if (isMine) Color.White.copy(alpha = 0.7f) else TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            message.text,
                            fontSize = 15.sp,
                            color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                formatMessageTime(message.createdAt),
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = if (isMine) TextAlign.End else TextAlign.Start
            )
        }
    }
}

private fun formatMessageTime(date: Date?): String {
    if (date == null) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1048576 -> "${bytes / 1024} KB"
        else -> "${" %.1f".format(bytes / 1048576.0)} MB"
    }
}

@Composable
private fun isSystemInDarkTheme(): Boolean {
    return androidx.compose.foundation.isSystemInDarkTheme()
}
