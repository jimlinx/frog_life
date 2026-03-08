package com.froglife.sync

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayInputStream

/**
 * Manages FCM notifications using HTTP v1 API (modern, secure approach)
 */
class FCMTokenManager(private val context: Context) {

    private val projectId = "frog-life-2f34c" // Replace with your Firebase project ID

    /**
     * Send FCM notification using HTTP v1 API with service account
     */
    suspend fun sendNotification(
        fcmToken: String,
        type: String,
        data: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken() ?: return@withContext

            val url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"

            val message = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", fcmToken)
                    put("data", JSONObject().apply {
                        put("type", type)
                        data.forEach { (key, value) -> put(key, value) }
                    })
                    // Optional: Add notification for visual display
                    put("notification", JSONObject().apply {
                        when (type) {
                            "changes_approved" -> {
                                put("title", "Sync Complete ✅")
                                put("body", "Latest changes synced from master device")
                            }
                            "changes_pending" -> {
                                put("title", "Pending Approvals 📋")
                                put("body", "${data["count"] ?: "1"} change(s) waiting for review")
                            }
                        }
                    })
                    // Android-specific options
                    put("android", JSONObject().apply {
                        put("priority", "high")
                    })
                })
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = message.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.e("FCM", "Failed to send notification: ${response.code} ${response.body?.string()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Error sending notification", e)
        }
    }

    /**
     * Get OAuth 2.0 access token from service account credentials
     */
    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            // Use the same GCS service account credentials
            val inputStream = context.assets.open("frog-sync-key.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            android.util.Log.e("FCM", "Failed to get access token", e)
            null
        }
    }
}
