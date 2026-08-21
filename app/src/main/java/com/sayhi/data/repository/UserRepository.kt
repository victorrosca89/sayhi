package com.sayhi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sayhi.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getUser(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserOnce(userId: String): User? {
        return try {
            firestore.collection("users").document(userId).get().await()
                .toObject(User::class.java)
        } catch (_: Exception) { null }
    }

    suspend fun searchUsers(query: String, currentUserId: String): List<User> {
        return try {
            firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(20).get().await()
                .documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.userId != currentUserId }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun updateUsername(userId: String, username: String): Result<Unit> = runCatching {
        firestore.collection("users").document(userId)
            .update("username", username).await()
    }

    suspend fun updateSettings(userId: String, darkMode: Boolean, notifSound: Boolean, ringtone: Boolean): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).update(
            mapOf(
                "dark_mode" to darkMode,
                "notification_sound" to notifSound,
                "ringtone_sound" to ringtone
            )
        ).await()
    }

    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "is_online" to isOnline,
                    "last_seen" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {}
    }

    fun getAllUsersFlow(currentUserId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) }
                    ?.filter { it.userId != currentUserId } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }
}