package com.example.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.UserPreferences
import java.util.Calendar

class NotificationHelper(
    private val context: Context,
    private val preferences: UserPreferences
) {
    private val channelId = "salim_responses_channel"
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Salim Responses"
            val descriptionText = "Notifications when long-running responses complete"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showResponseCompleted(conversationId: String, title: String, snippet: String) {
        if (!preferences.notificationsEnabled.value) return
        if (isInQuietHours()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_id", conversationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.salim_icon)
            .setContentTitle(title)
            .setContentText(snippet)
            .setStyle(NotificationCompat.BigTextStyle().bigText(snippet))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(conversationId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Missing runtime permission on Android 13+
        }
    }

    private fun isInQuietHours(): Boolean {
        if (!preferences.quietHoursEnabled.value) return false
        val startStr = preferences.quietHoursStart.value // "22:00"
        val endStr = preferences.quietHoursEnd.value // "07:00"

        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val startParts = startStr.split(":")
        val startMin = startParts.getOrNull(0)?.toIntOrNull()?.times(60)?.plus(startParts.getOrNull(1)?.toIntOrNull() ?: 0) ?: 22 * 60

        val endParts = endStr.split(":")
        val endMin = endParts.getOrNull(0)?.toIntOrNull()?.times(60)?.plus(endParts.getOrNull(1)?.toIntOrNull() ?: 0) ?: 7 * 60

        return if (startMin <= endMin) {
            currentMinutes in startMin..endMin
        } else {
            // Spans midnight, e.g. 22:00 to 07:00
            currentMinutes >= startMin || currentMinutes <= endMin
        }
    }
}
