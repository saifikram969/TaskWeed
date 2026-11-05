package com.saif.scheduleapp.reminder

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.util.reminderSpeech

class ReminderService : Service() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DETAILS = "extra_task_details"
        const val EXTRA_TASK_START_MINUTES = "extra_task_start_minutes"
        const val EXTRA_TASK_END_MINUTES = "extra_task_end_minutes"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
        const val EXTRA_SPEAK = "extra_speak"

        private const val NOTIFICATION_ID_BASE = 10000
        private const val SERVICE_TIMEOUT_MS = 15000L
    }

    private var speaker: AndroidSpeaker? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isStopping = false

    private val timeoutRunnable = Runnable {
        Log.w("ReminderService", "Safety timeout reached; terminating ReminderService.")
        finishService()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
        val details = intent.getStringExtra(EXTRA_TASK_DETAILS) ?: ""
        val startMinutes = intent.getIntExtra(EXTRA_TASK_START_MINUTES, 0)
        val endMinutes = intent.getIntExtra(EXTRA_TASK_END_MINUTES, 0)
        val speak = intent.getBooleanExtra(EXTRA_SPEAK, true)
        val categoryName = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: Category.LEARNING.name
        val category = try {
            Category.valueOf(categoryName)
        } catch (_: Exception) {
            Category.LEARNING
        }

        val task = ScheduleTask(
            id = taskId,
            title = title,
            details = details,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            category = category,
            voiceEnabled = speak
        )

        // Ensure notification channel exists
        NotificationChannels.createReminderChannel(this)

        val notificationId = NOTIFICATION_ID_BASE + (if (taskId > 0) (taskId % 10000).toInt() else startId)

        // Full-screen intent: open ReminderActivity (including when device is locked)
        val reminderActivityIntent = Intent().apply {
            setClassName(packageName, "com.example.taskweed.ReminderActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_DETAILS, details)
            putExtra(EXTRA_TASK_START_MINUTES, startMinutes)
            putExtra(EXTRA_TASK_END_MINUTES, endMinutes)
            putExtra(EXTRA_TASK_CATEGORY, categoryName)
            putExtra(EXTRA_SPEAK, speak)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            reminderActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        @Suppress("DEPRECATION")
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NotificationChannels.REMINDERS_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
            .setContentTitle(title.ifBlank { "Task Reminder" })
            .setContentText(details.ifBlank { "Time for your scheduled task!" })
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCategory(Notification.CATEGORY_REMINDER)
                    setVisibility(Notification.VISIBILITY_PUBLIC)
                }
            }
            .build()

        // Start foreground service with SHORT_SERVICE type on Android 14+ (targetSdk 37)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, 0)
        } else {
            startForeground(notificationId, notification)
        }

        // Arm safety timeout to ensure shortService stops well before the 3-minute Android system limit
        mainHandler.postDelayed(timeoutRunnable, SERVICE_TIMEOUT_MS)

        // Speak only when speak is true
        if (speak) {
            val speechText = reminderSpeech(task)
            speaker = AndroidSpeaker(this) {
                Log.d("ReminderService", "Speech completed for task '$title'.")
                finishService()
            }
            speaker?.speak(speechText)
        } else {
            Log.d("ReminderService", "Speech disabled for task '$title'. Skipping speech.")
            mainHandler.postDelayed({ finishService() }, 1000L)
        }

        return START_NOT_STICKY
    }

    private fun finishService() {
        if (isStopping) return
        isStopping = true

        mainHandler.removeCallbacks(timeoutRunnable)

        try {
            speaker?.shutdown()
            speaker = null
        } catch (e: Exception) {
            Log.w("ReminderService", "Error during speaker shutdown: ${e.message}")
        }

        // Detach notification so user keeps it visible in the notification drawer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }

        stopSelf()
    }

    override fun onDestroy() {
        finishService()
        super.onDestroy()
    }
}
