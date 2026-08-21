package com.sayhi.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayhi.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurplePrimary, PurpleDark, DarkBgSecondary)
                )
            ),
        contentAlignment = Alignment.Center
    )
    {
        // Floating circles decoration
        FloatingCircle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 40.dp),
            size = 120.dp,
            color = PurpleLight.copy(alpha = 0.15f)
        )
        FloatingCircle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-50).dp),
            size = 80.dp,
            color = PurpleVivid.copy(alpha = 0.1f)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = PurpleDark.copy(alpha = 0.3f), spotColor = PurpleDark.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 36.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, ambientColor = PurplePrimary.copy(alpha = 0.4f), spotColor = PurplePrimary.copy(alpha = 0.4f))
                        .background(
                            Brush.linearGradient(colors = listOf(PurplePrimary, PurpleVivid)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChatBubble, contentDescription = null,
                        modifier = Modifier.size(36.dp), tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "SayHi!", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Text(
                    if (isSignUp) "Create your account" else "Welcome back",
                    fontSize = 14.sp, color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            focusedLabelColor = PurplePrimary,
                            cursorColor = PurplePrimary
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PurplePrimary) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary,
                        cursorColor = PurplePrimary
                    ),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = PurplePrimary) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        focusedLabelColor = PurplePrimary,
                        cursorColor = PurplePrimary
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PurplePrimary) }
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = RedDanger, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.signUp(username, email, password, onLoginSuccess)
                        } else {
                            viewModel.signIn(email, password, onLoginSuccess)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = PurplePrimary.copy(alpha = 0.4f), spotColor = PurplePrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White, strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (isSignUp) "Sign Up" else "Sign In",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        if (isSignUp) "Already have an account? Sign In"
                        else "Don't have an account? Sign Up",
                        color = PurplePrimary, fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { viewModel.signInAnonymously(onLoginSuccess) }) {
                    Text(
                        "Skip — Continue as Guest",
                        color = TextSecondary, fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingCircle(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "offset"
    )
    Box(
        modifier = modifier
            .size(size)
            .offset(y = offset.dp)
            .background(color, CircleShape)
    )
}
