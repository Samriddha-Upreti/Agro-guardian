package com.example.agribuddy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationManagerSingleton {

    private const val CHANNEL_ID = "default_channel"
    private const val GROUP_KEY = "group_key_notifications"

    // List to store notifications
    private val notificationList = mutableListOf<NotificationData>()

    // Generate unique notification ID
    private fun generateNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    // Add a notification
    fun addNotification(context: Context, title: String, message: String) {
        val id = generateNotificationId()  // Auto-generate unique ID
        notificationList.add(NotificationData(id, title, message))
        showGroupedNotifications(context)
    }

    // Show grouped notifications
    private fun showGroupedNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Default Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // Create individual notifications and add them to the group
        for (notificationData in notificationList) {
            val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(notificationData.title)
                .setContentText(notificationData.message)
                .setSmallIcon(R.drawable.ic_notifications)
                .setGroup(GROUP_KEY)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            // Display the individual notification
            notificationManager.notify(notificationData.id, notification)
        }

        // Create a summary notification for the group
        val summaryNotification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("You have ${notificationList.size} new notifications")
            .setContentText("Tap to view all updates")
            .setSmallIcon(R.drawable.ic_notifications)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Display the summary notification (this represents the whole group)
        notificationManager.notify(0, summaryNotification) // 0 is the ID for the summary notification
    }
}

// Data class to represent a notification
data class NotificationData(val id: Int, val title: String, val message: String)
