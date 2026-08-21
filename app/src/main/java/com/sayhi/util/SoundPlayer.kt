package com.sayhi.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.sayhi.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(private val context: Context) {

    private var notificationPlayer: MediaPlayer? = null
    private var ringtonePlayer: MediaPlayer? = null

    fun playNotification() {
        try {
            notificationPlayer?.release()
            notificationPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.notification_sound}"))
                prepareAsync()
                setOnPreparedListener { it.start() }
                setOnCompletionListener { it.release(); notificationPlayer = null }
            }
        } catch (_: Exception) {}
    }

    fun playRingtone() {
        try {
            ringtonePlayer?.release()
            ringtonePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, Uri.parse("android.resource://${context.packageName}/${R.raw.ringtone_sound}"))
                isLooping = true
                prepareAsync()
                setOnPreparedListener { it.start() }
            }
        } catch (_: Exception) {}
    }

    fun stopRingtone() {
        ringtonePlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        ringtonePlayer = null
    }
}
