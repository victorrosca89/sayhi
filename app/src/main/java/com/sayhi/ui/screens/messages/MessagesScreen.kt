package com.sayhi.ui.screens.messages

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.data.model.ChatInfo
import com.sayhi.data.model.User
import com.sayhi.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesScreen(
    onChatClick: (userId: String, username: String, avatarUrl: String) -> Unit,
    onSettingsClick: () -> Unit,
    onVoiceCall: (userId: String, username: String) -> Unit,
    onVideoCall: (userId: String, username: String) -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val users by viewModel.users.collectAsState()
    val pinnedChats = chats.filter { it.isPinned }
    val allChats = chats.filter { !it.isPinned }
    val totalUnread = chats.sumOf { it.unreadCount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* New chat */ },
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape, ambientColor = PurplePrimary.copy(alpha = 0.4f), spotColor = PurplePrimary.copy(alpha = 0.4f)),
                containerColor = PurplePrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Purple header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PurplePrimary, PurpleVivid, PurpleDark)
                        )
                    )
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "You Received",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp, fontWeight = FontWeight.Normal
                            )
                            Text(
                                "$totalUnread Messages",
                                color = Color.White,
                                fontSize = 28.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Contact List",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp, fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White, modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal avatar scroll
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(users.take(8), key = { it.userId }) { user ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = PurpleLight.copy(alpha = 0.3f)
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    if (user.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(12.dp)
                                                .background(GreenOnline, CircleShape)
                                                .border(
                                                    2.dp, Color.White, CircleShape
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    user.username,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(56.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Direct Message", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = PurplePrimary, modifier = Modifier.size(20.dp))
                },
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary.copy(alpha = 0.3f),
                    cursorColor = PurplePrimary
                )
            )

            // Chat lists
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (pinnedChats.isNotEmpty()) {
                    item {
                        Text(
                            "Pinned Message (${pinnedChats.size})",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pinnedChats, key = { it.chatId }) { chat ->
                        ChatItem(
                            chat = chat,
                            users = users,
                            currentUserId = viewModel.currentUserId,
                            onClick = { userId, name, avatar ->
                                onChatClick(userId, name, avatar)
                            }
                        )
                    }
                }

                if (allChats.isNotEmpty()) {
                    item {
                        Text(
                            "All Message (${allChats.size})",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(allChats, key = { it.chatId }) { chat ->
                        ChatItem(
                            chat = chat,
                            users = users,
                            currentUserId = viewModel.currentUserId,
                            onClick = { userId, name, avatar ->
                                onChatClick(userId, name, avatar)
                            }
                        )
                    }
                }

                if (chats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No conversations yet.\nTap + to start chatting!",
                                color = TextSecondary,
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatItem(
    chat: ChatInfo,
    users: List<User>,
    currentUserId: String,
    onClick: (String, String, String) -> Unit
) {
    val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return
    val otherUser = users.find { it.userId == otherUserId }

    Card(
        onClick = { onClick(otherUserId, otherUser?.username ?: "", otherUser?.avatarUrl ?: "") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = PurpleLight.copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Default.Person, null,
                        tint = PurplePrimary, modifier = Modifier.size(28.dp)
                    )
                }
                if (otherUser?.isOnline == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .background(GreenOnline, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    otherUser?.username ?: "Unknown",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    chat.lastMessage,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatTime(chat.lastMessageTime),
                    fontSize = 11.sp, color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (chat.unreadCount > 0) {
                    Badge(
                        containerColor = RedBadge,
                        contentColor = Color.White
                    ) {
                        Text("${chat.unreadCount}", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

private fun formatTime(date: java.util.Date?): String {
    if (date == null) return ""
    val now = java.util.Date()
    val diff = now.time - date.time
    return when {
        diff < 86400000 -> java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
        diff < 172800000 -> "Yesterday"
        else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(date)
    }
}
