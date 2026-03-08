package com.froglife.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.froglife.MainActivity
import com.froglife.R
import com.froglife.data.FrogDatabase
import com.froglife.data.FrogRepository
import com.froglife.utils.ExportData
import com.froglife.utils.importAllData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrogSyncMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "frog_sync_channel"
        private const val NOTIFICATION_ID_SYNC = 1001
        private const val NOTIFICATION_ID_PENDING = 1002
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"] ?: return

        when (type) {
            "changes_approved" -> handleChangesApproved(message.data)
            "changes_pending" -> handleChangesPending(message.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Save token to SharedPreferences
        getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()

        // Upload token to GCS in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = FrogRepository(FrogDatabase.getDatabase(applicationContext))
                val syncService = GCSSyncService(applicationContext, repository)
                syncService.uploadDeviceToken(token)
            } catch (e: Exception) {
                // Silently fail - token will be uploaded on next sync
            }
        }
    }

    private fun handleChangesApproved(data: Map<String, String>) {
        // Auto-sync in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = FrogRepository(FrogDatabase.getDatabase(applicationContext))
                val syncService = GCSSyncService(applicationContext, repository)

                val approvedData = syncService.downloadApprovedData()
                if (approvedData != null) {
                    // Import data silently
                    importAllData(repository, approvedData)

                    // Show success notification
                    showNotification(
                        title = "Sync Complete ✅",
                        message = "Latest changes synced from master device",
                        notificationId = NOTIFICATION_ID_SYNC
                    )
                }
            } catch (e: Exception) {
                showNotification(
                    title = "Sync Failed",
                    message = "Could not sync data: ${e.message}",
                    notificationId = NOTIFICATION_ID_SYNC
                )
            }
        }
    }

    private fun handleChangesPending(data: Map<String, String>) {
        val count = data["count"] ?: "1"

        showNotification(
            title = "Pending Approvals 📋",
            message = "$count change(s) waiting for your review",
            notificationId = NOTIFICATION_ID_PENDING
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Frog Life Sync",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for multi-device synchronization"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
