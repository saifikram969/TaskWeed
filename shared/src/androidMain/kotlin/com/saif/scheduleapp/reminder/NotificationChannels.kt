package com.saif.scheduleapp.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannels {
    const val REMINDERS_CHANNEL_ID = "task_reminders_channel"
    const val REMINDERS_CHANNEL_NAME = "Task Reminders"

    const val SILENT_SERVICE_CHANNEL_ID = "task_reminders_silent_channel"
    const val SILENT_SERVICE_CHANNEL_NAME = "Task Reminders (Active Service)"

    /**
     * Creates both high-priority alert and low-priority silent notification channels
     * on Android O (API 26) and above.
     */
    fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val reminderChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                REMINDERS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications and alerts for scheduled tasks"
                enableVibration(true)
                enableLights(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            val silentChannel = NotificationChannel(
                SILENT_SERVICE_CHANNEL_ID,
                SILENT_SERVICE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active reminder service notifications"
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(reminderChannel)
            notificationManager?.createNotificationChannel(silentChannel)
        }
    }
}
