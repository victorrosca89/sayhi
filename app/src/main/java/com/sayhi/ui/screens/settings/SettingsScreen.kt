package com.sayhi.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val username by viewModel.username.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notifSound by viewModel.notifSound.collectAsState()
    val ringtone by viewModel.ringtone.collectAsState()
    var showEditName by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf(username) }

    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        cursorColor = PurplePrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUsername(editNameText)
                    showEditName = false
                }) { Text("Save", color = PurplePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Profile avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(16.dp, CircleShape, PurplePrimary.copy(alpha = 0.3f))
                    .border(3.dp, PurplePrimary, CircleShape)
                    .clip(CircleShape)
                    .background(PurpleLight.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(44.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text(username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = {
                editNameText = username
                showEditName = true
            }) {
                Text("Edit Profile", color = PurplePrimary, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Settings sections
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Switch between dark and light theme",
                    checked = isDarkMode,
                    onToggle = { viewModel.toggleDarkMode() }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Message Notifications",
                    subtitle = "Play sound for new messages",
                    checked = notifSound,
                    onToggle = { viewModel.toggleNotifSound() }
                )
                SettingsToggleItem(
                    icon = Icons.Default.RingVolume,
                    title = "Call Ringtone",
                    subtitle = "Play ringtone for incoming calls",
                    checked = ringtone,
                    onToggle = { viewModel.toggleRingtone() }
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "Server") {
                SettingsTextItem(
                    icon = Icons.Default.Dns,
                    title = "Signaling Server",
                    subtitle = viewModel.serverUrl
                )
            }

            Spacer(Modifier.height(32.dp))

            // Sign out
            OutlinedButton(
                onClick = { viewModel.signOut() },
                modifier = Modifier.fillMaxWidth(0.6f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RedDanger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = PurplePrimary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PurplePrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = PurplePrimary, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun SettingsTextItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PurplePrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
