package com.sayhi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sayhi.ui.screens.auth.LoginScreen
import com.sayhi.ui.screens.calls.VideoCallScreen
import com.sayhi.ui.screens.calls.VoiceCallScreen
import com.sayhi.ui.screens.chat.ChatScreen
import com.sayhi.ui.screens.messages.MessagesScreen
import com.sayhi.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Messages : Screen("messages")
    data object Chat : Screen("chat/{userId}/{username}/{avatarUrl}") {
        fun createRoute(userId: String, username: String, avatarUrl: String = "") =
            "chat/$userId/$username/$avatarUrl"
    }
    data object VoiceCall : Screen("voice_call/{callerId}/{callerName}/{isOutgoing}") {
        fun createRoute(callerId: String, callerName: String, isOutgoing: Boolean = true) =
            "voice_call/$callerId/$callerName/$isOutgoing"
    }
    data object VideoCall : Screen("video_call/{callerId}/{callerName}/{isOutgoing}") {
        fun createRoute(callerId: String, callerName: String, isOutgoing: Boolean = true) =
            "video_call/$callerId/$callerName/$isOutgoing"
    }
    data object Settings : Screen("settings")
}

@Composable
fun SayHiNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Messages.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                onChatClick = { userId, username, avatarUrl ->
                    navController.navigate(Screen.Chat.createRoute(userId, username, avatarUrl))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onVoiceCall = { callerId, callerName ->
                    navController.navigate(Screen.VoiceCall.createRoute(callerId, callerName))
                },
                onVideoCall = { callerId, callerName ->
                    navController.navigate(Screen.VideoCall.createRoute(callerId, callerName))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
                navArgument("avatarUrl") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val avatarUrl = backStackEntry.arguments?.getString("avatarUrl") ?: ""

            ChatScreen(
                userId = userId,
                username = username,
                avatarUrl = avatarUrl,
                onBackClick = { navController.popBackStack() },
                onVoiceCall = { callerId, callerName ->
                    navController.navigate(Screen.VoiceCall.createRoute(callerId, callerName))
                },
                onVideoCall = { callerId, callerName ->
                    navController.navigate(Screen.VideoCall.createRoute(callerId, callerName))
                }
            )
        }

        composable(
            route = Screen.VoiceCall.route,
            arguments = listOf(
                navArgument("callerId") { type = NavType.StringType },
                navArgument("callerName") { type = NavType.StringType },
                navArgument("isOutgoing") { type = NavType.BoolType; defaultValue = true }
            )
        ) { backStackEntry ->
            val callerId = backStackEntry.arguments?.getString("callerId") ?: ""
            val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
            val isOutgoing = backStackEntry.arguments?.getBoolean("isOutgoing") ?: true

            VoiceCallScreen(
                callerId = callerId,
                callerName = callerName,
                isOutgoing = isOutgoing,
                onEndCall = { navController.popBackStack() },
                onSwitchToVideo = { cId, cName ->
                    navController.navigate(Screen.VideoCall.createRoute(cId, cName, false))
                }
            )
        }

        composable(
            route = Screen.VideoCall.route,
            arguments = listOf(
                navArgument("callerId") { type = NavType.StringType },
                navArgument("callerName") { type = NavType.StringType },
                navArgument("isOutgoing") { type = NavType.BoolType; defaultValue = true }
            )
        ) { backStackEntry ->
            val callerId = backStackEntry.arguments?.getString("callerId") ?: ""
            val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
            val isOutgoing = backStackEntry.arguments?.getBoolean("isOutgoing") ?: true

            VideoCallScreen(
                callerId = callerId,
                callerName = callerName,
                isOutgoing = isOutgoing,
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
