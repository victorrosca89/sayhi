package com.sayhi.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sayhi.MainActivity
import com.sayhi.R
import com.sayhi.SayHiApp

class SayHiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"] ?: "message"
        val senderName = message.data["sender_name"] ?: "Someone"
        val body = message.data["body"] ?: ""
        val senderId = message.data["sender_id"] ?: ""

        when (type) {
            "message" -> showMessageNotification(senderName, body, senderId)
            "call" -> showCallNotification(senderName, senderId, message.data["call_type"] ?: "voice")
        }
    }

    override fun onNewToken(token: String) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return)
            .update("fcm_token", token)
    }

    private fun showMessageNotification(sender: String, body: String, senderId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_chat", true)
            putExtra("user_id", senderId)
            putExtra("username", sender)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, SayHiApp.CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(sender)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(100, 200, 100, 200))
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(senderId.hashCode(), notification)
    }

    private fun showCallNotification(callerName: String, callerId: String, callType: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("incoming_call", true)
            putExtra("caller_id", callerId)
            putExtra("caller_name", callerName)
            putExtra("call_type", callType)
        }
        val pendingIntent = PendingIntent.getActivity(this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, SayHiApp.CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Incoming $callType call")
            .setContentText(callerName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }
}