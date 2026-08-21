package com.sayhi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.sayhi.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null

    suspend fun signInAnonymously(): Result<User> = runCatching {
        val result = auth.signInAnonymously().await()
        val user = User(
            userId = result.user?.uid ?: throw Exception("Auth failed"),
            username = "User_${result.user?.uid?.take(6) ?: "unknown"}",
            fcmToken = getFcmToken()
        )
        firestore.collection("users").document(user.userId).set(user).await()
        user
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = getUserFromFirestore(result.user?.uid ?: throw Exception("No user"))
        updateFcmToken(user.userId, getFcmToken())
        user
    }

    suspend fun signUpWithEmail(username: String, email: String, password: String): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = User(
            userId = result.user?.uid ?: throw Exception("No user"),
            username = username,
            email = email,
            fcmToken = getFcmToken()
        )
        firestore.collection("users").document(user.userId).set(user).await()
        user
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: throw Exception("No user")
        val existing = firestore.collection("users").document(uid).get().await()
        if (existing.exists()) {
            getUserFromFirestore(uid).also { updateFcmToken(uid, getFcmToken()) }
        } else {
            val user = User(
                userId = uid,
                username = result.user?.displayName ?: "User",
                email = result.user?.email ?: "",
                avatarUrl = result.user?.photoUrl?.toString() ?: "",
                fcmToken = getFcmToken()
            )
            firestore.collection("users").document(uid).set(user).await()
            user
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun getUserFromFirestore(uid: String): User {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java) ?: throw Exception("User not found")
    }

    private suspend fun getFcmToken(): String {
        return try { messaging.token.await() } catch (_: Exception) { "" }
    }

    private suspend fun updateFcmToken(uid: String, token: String) {
        if (token.isNotEmpty()) {
            firestore.collection("users").document(uid)
                .update("fcm_token", token).await()
        }
    }
}