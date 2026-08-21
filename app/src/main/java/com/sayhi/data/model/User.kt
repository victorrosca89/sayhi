package com.sayhi.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class User(
    @get:PropertyName("user_id") @set:PropertyName("user_id")
    var userId: String = "",
    @get:PropertyName("username") @set:PropertyName("username")
    var username: String = "",
    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",
    @get:PropertyName("avatar_url") @set:PropertyName("avatar_url")
    var avatarUrl: String = "",
    @get:PropertyName("fcm_token") @set:PropertyName("fcm_token")
    var fcmToken: String = "",
    @get:PropertyName("is_online") @set:PropertyName("is_online")
    var isOnline: Boolean = false,
    @get:PropertyName("last_seen") @set:PropertyName("last_seen")
    @ServerTimestamp var lastSeen: Date? = null,
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    @ServerTimestamp var createdAt: Date? = null,
    @get:PropertyName("dark_mode") @set:PropertyName("dark_mode")
    var darkMode: Boolean = true,
    @get:PropertyName("notification_sound") @set:PropertyName("notification_sound")
    var notificationSound: Boolean = true,
    @get:PropertyName("ringtone_sound") @set:PropertyName("ringtone_sound")
    var ringtoneSound: Boolean = true
)