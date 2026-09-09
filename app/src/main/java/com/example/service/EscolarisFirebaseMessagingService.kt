package com.example.service

import android.content.Context
import android.util.Log
import com.example.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EscolarisFirebaseMessagingService : FirebaseMessagingService() {

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
        saveTokenLocally(token)
        syncTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Extraer título y mensaje (preferir notification payload, fallback a data payload)
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Escolaris - Plataforma Escolar"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "Tienes una nueva actualización escolar"

        val type = remoteMessage.data["type"] ?: "GENERAL"
        val notificationId = (remoteMessage.data["id"]?.toIntOrNull()) ?: (1000..9999).random()

        // 2. Mostrar la notificación con el NotificationHelper nativo
        NotificationHelper.showPushNotification(
            context = applicationContext,
            id = notificationId,
            title = title,
            message = body,
            type = type
        )
    }

    private fun saveTokenLocally(token: String) {
        try {
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token locally", e)
        }
    }

    private fun syncTokenToFirestore(token: String) {
        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: run {
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.getString("saved_user_id", null)
            }

            if (!userId.isNullOrBlank()) {
                val store = FirebaseFirestore.getInstance()
                val tokenData = hashMapOf(
                    "token" to token,
                    "platform" to "android",
                    "deviceModel" to android.os.Build.MODEL,
                    "updatedAt" to System.currentTimeMillis()
                )

                store.collection("users")
                    .document(userId)
                    .collection("fcmTokens")
                    .document(token)
                    .set(tokenData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token synced successfully to Firestore users/$userId/fcmTokens")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to sync FCM token to Firestore", e)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in syncTokenToFirestore", e)
        }
    }

    companion object {
        private const val TAG = "EscolarisFCM"
        private const val PREFS_NAME = "escolaris_session"
        private const val KEY_FCM_TOKEN = "fcm_push_token"

        /**
         * Helper estático para sincronizar el token guardado una vez el usuario inicia sesión.
         */
        fun syncCurrentUserToken(context: Context, userId: String) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val token = prefs.getString(KEY_FCM_TOKEN, null)
                if (!token.isNullOrBlank() && userId.isNotBlank()) {
                    val store = FirebaseFirestore.getInstance()
                    val tokenData = hashMapOf(
                        "token" to token,
                        "platform" to "android",
                        "deviceModel" to android.os.Build.MODEL,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    store.collection("users")
                        .document(userId)
                        .collection("fcmTokens")
                        .document(token)
                        .set(tokenData, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in syncCurrentUserToken", e)
            }
        }
    }
}
