package com.sayhi.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.sayhi.R
import com.sayhi.SayHiApp

class CallService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerName = intent?.getStringExtra("caller_name") ?: "Unknown"
        val callType = intent?.getStringExtra("call_type") ?: "voice"

        val notification = NotificationCompat.Builder(this, SayHiApp.CHANNEL_CALLS)
            .setContentTitle("Ongoing $callType call")
            .setContentText(callerName)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(1002, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}