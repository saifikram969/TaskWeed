package com.saif.scheduleapp.reminder

import android.app.ActivityManager
import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.util.reminderSpeech
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class ReminderService : Service() {

    interface SpeechCallback {
        fun onSpeechStarted()
        fun onSpeechFinished()
        fun onSpeechError(errorCode: Int?)
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DETAILS = "extra_task_details"
        const val EXTRA_TASK_START_MINUTES = "extra_task_start_minutes"
        const val EXTRA_TASK_END_MINUTES = "extra_task_end_minutes"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
        const val EXTRA_SPEAK = "extra_speak"

        const val ACTION_ACTIVITY_READY = "com.saif.scheduleapp.reminder.ACTION_ACTIVITY_READY"
        const val ACTION_STOP_REMINDER = "com.saif.scheduleapp.reminder.ACTION_STOP_REMINDER"

        private const val NOTIFICATION_ID_BASE = 10000
        private const val SERVICE_TIMEOUT_MS = 25000L
        private const val LOCKSCREEN_FALLBACK_TIMEOUT_MS = 5000L
        private const val FOREGROUND_FALLBACK_TIMEOUT_MS = 3000L

        @Volatile
        private var activeService: WeakReference<ReminderService>? = null

        @Volatile
        var currentTaskId: Long = -1L
            private set

        @Volatile
        var isCurrentlySpeaking: Boolean = false
            private set

        private val listeners = CopyOnWriteArrayList<SpeechCallback>()

        fun registerSpeechCallback(taskId: Long, callback: SpeechCallback) {
            if (!listeners.contains(callback)) {
                listeners.add(callback)
            }
            // If speech is ALREADY in progress for this task, immediately notify callback
            if (currentTaskId == taskId && isCurrentlySpeaking) {
                callback.onSpeechStarted()
            }
        }

        fun unregisterSpeechCallback(callback: SpeechCallback) {
            listeners.remove(callback)
        }

        fun onActivityReady(taskId: Long) {
            val service = activeService?.get()
            if (service != null && service.activeTaskId == taskId) {
                service.mainHandler.post {
                    service.onActivityReadyInternal()
                }
            }
        }

        fun stopSpeechAndFinish(taskId: Long = -1L) {
            val service = activeService?.get()
            if (service != null && (service.activeTaskId == taskId || taskId == -1L)) {
                service.mainHandler.post {
                    service.finishService()
                }
            }
        }

        fun isSpeaking(taskId: Long): Boolean {
            return currentTaskId == taskId && isCurrentlySpeaking
        }

        private fun isAppInForeground(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            val appProcesses = activityManager.runningAppProcesses ?: return false
            val packageName = context.packageName
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName == packageName) {
                    return true
                }
            }
            return false
        }
    }

    private var speaker: AndroidSpeaker? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isStopping = false

    var activeTaskId: Long = -1L
        private set

    private var currentNotificationId: Int = NOTIFICATION_ID_BASE
    private var currentTaskTitle: String = ""
    private var currentTaskDetails: String = ""
    private var currentFullScreenPendingIntent: PendingIntent? = null

    private val speechTriggered = AtomicBoolean(false)
    private var speechTextToSpeak: String? = null
    private var taskSpeakEnabled: Boolean = false

    private val fallbackRunnable = Runnable {
        Log.d("ReminderService", "Safety fallback timeout reached without Activity readiness; starting voice playback.")
        triggerSpeech()
    }

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

        // Handle explicit command actions if sent
        if (intent.action == ACTION_ACTIVITY_READY) {
            val readyTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            onActivityReady(readyTaskId)
            return START_NOT_STICKY
        }
        if (intent.action == ACTION_STOP_REMINDER) {
            val stopTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            stopSpeechAndFinish(stopTaskId)
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

        activeTaskId = taskId
        currentTaskId = taskId
        currentTaskTitle = title
        currentTaskDetails = details
        activeService = WeakReference(this)
        isCurrentlySpeaking = false
        speechTriggered.set(false)
        taskSpeakEnabled = speak
        isStopping = false

        // Ensure notification channels exist
        NotificationChannels.createReminderChannel(this)

        val notificationId = NOTIFICATION_ID_BASE + (if (taskId > 0) (taskId % 10000).toInt() else startId)
        currentNotificationId = notificationId

        // Evaluate device state to handle Scenarios 1, 2, and 3
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isInteractive = powerManager?.isInteractive ?: false
        val isKeyguardLocked = keyguardManager?.isKeyguardLocked ?: false
        val isAppForeground = isAppInForeground(this)

        Log.d(
            "ReminderService",
            "Trigger reminder for task $taskId: isInteractive=$isInteractive, isKeyguardLocked=$isKeyguardLocked, isAppForeground=$isAppForeground"
        )

        // Intent to open ReminderActivity
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

        // Background activity launch allowance options on Android 14+ (targetSdk 37)
        @Suppress("DEPRECATION")
        val activityOptionsBundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions.makeBasic().apply {
                setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            }.toBundle()
        } else {
            null
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            reminderActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            activityOptionsBundle
        )
        currentFullScreenPendingIntent = fullScreenPendingIntent

        // SCENARIO 2: TaskWeed is in the foreground
        // Open ReminderActivity directly. Do not post an alert notification or heads-up banner.
        // Fulfill foreground service requirements using a silent background notification.
        if (isInteractive && !isKeyguardLocked && isAppForeground) {
            Log.d("ReminderService", "Scenario 2: TaskWeed is in foreground. Starting ReminderActivity directly and using silent foreground notification.")
            try {
                startActivity(reminderActivityIntent)
            } catch (e: Exception) {
                Log.e("ReminderService", "Failed to launch ReminderActivity directly: ${e.message}", e)
            }

            val silentNotification = buildSilentNotification(taskId, title, details, fullScreenPendingIntent)
            startForegroundServiceWithNotification(notificationId, silentNotification)
            mainHandler.postDelayed(fallbackRunnable, FOREGROUND_FALLBACK_TIMEOUT_MS)

        } else if (!isInteractive || isKeyguardLocked) {
            // SCENARIO 1: Device Screen OFF / Locked
            // Post high-priority alert notification with fullScreenIntent so Android wakes the screen
            // and presents ReminderActivity above keyguard.
            Log.d("ReminderService", "Scenario 1: Locked or screen off. Posting full-screen intent alert notification.")
            val alertNotification = buildAlertNotification(notificationId, taskId, title, details, fullScreenPendingIntent)
            startForegroundServiceWithNotification(notificationId, alertNotification)
            mainHandler.postDelayed(fallbackRunnable, LOCKSCREEN_FALLBACK_TIMEOUT_MS)

        } else {
            // SCENARIO 3: Another app in foreground (e.g. YouTube)
            // Post high-priority Heads-Up Notification with actions [Open, Snooze 5m, Start now].
            // Suppress fallback voice so TTS never speaks into the void without dialogue.
            Log.d("ReminderService", "Scenario 3: Another app is in foreground. Posting Heads-Up Notification with actions; suppressing fallback voice.")
            val alertNotification = buildAlertNotification(notificationId, taskId, title, details, fullScreenPendingIntent)
            startForegroundServiceWithNotification(notificationId, alertNotification)
        }

        // Arm safety timeout to ensure shortService stops well before the 3-minute Android system limit
        mainHandler.postDelayed(timeoutRunnable, SERVICE_TIMEOUT_MS)

        // Configure speech if enabled
        if (speak) {
            speechTextToSpeak = reminderSpeech(task)

            speaker = AndroidSpeaker(this, object : SpeechListener {
                override fun onSpeechStarted() {
                    Log.d("ReminderService", "Speech started for task '$title'.")
                    isCurrentlySpeaking = true
                    for (cb in listeners) {
                        try {
                            cb.onSpeechStarted()
                        } catch (e: Exception) {
                            Log.w("ReminderService", "Error in onSpeechStarted callback: ${e.message}")
                        }
                    }
                }

                override fun onSpeechFinished() {
                    Log.d("ReminderService", "Speech finished for task '$title'.")
                    isCurrentlySpeaking = false
                    for (cb in listeners) {
                        try {
                            cb.onSpeechFinished()
                        } catch (e: Exception) {
                            Log.w("ReminderService", "Error in onSpeechFinished callback: ${e.message}")
                        }
                    }
                    mainHandler.postDelayed({ finishService() }, 1500L)
                }

                override fun onSpeechError(errorCode: Int?) {
                    Log.e("ReminderService", "Speech error for task '$title', code: $errorCode")
                    isCurrentlySpeaking = false
                    for (cb in listeners) {
                        try {
                            cb.onSpeechError(errorCode)
                        } catch (e: Exception) {
                            Log.w("ReminderService", "Error in onSpeechError callback: ${e.message}")
                        }
                    }
                    mainHandler.postDelayed({ finishService() }, 1000L)
                }
            })
        } else {
            Log.d("ReminderService", "Speech disabled for task '$title'. Skipping speech.")
            mainHandler.postDelayed({ finishService() }, 1000L)
        }

        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification(notificationId: Int, notification: Notification) {
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
    }

    private fun buildAlertNotification(
        notificationId: Int,
        taskId: Long,
        title: String,
        details: String,
        fullScreenPendingIntent: PendingIntent
    ): Notification {
        val openAction = Notification.Action.Builder(
            null,
            "Open",
            fullScreenPendingIntent
        ).build()

        val snoozeIntent = Intent(this, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE_TASK
            putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeAction = Notification.Action.Builder(
            null,
            "Snooze 5m",
            snoozePendingIntent
        ).build()

        val startIntent = Intent(this, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_START_TASK
            putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 2,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val startAction = Notification.Action.Builder(
            null,
            "Start now",
            startPendingIntent
        ).build()

        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NotificationChannels.REMINDERS_CHANNEL_ID)
        } else {
            Notification.Builder(this)
                .setPriority(Notification.PRIORITY_HIGH)
        }

        return builder
            .setContentTitle(title.ifBlank { "Task Reminder" })
            .setContentText(details.ifBlank { "Time for your scheduled task!" })
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .addAction(openAction)
            .addAction(snoozeAction)
            .addAction(startAction)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCategory(Notification.CATEGORY_REMINDER)
                    setVisibility(Notification.VISIBILITY_PUBLIC)
                }
            }
            .build()
    }

    private fun buildSilentNotification(
        taskId: Long,
        title: String,
        details: String,
        fullScreenPendingIntent: PendingIntent
    ): Notification {
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NotificationChannels.SILENT_SERVICE_CHANNEL_ID)
        } else {
            Notification.Builder(this)
                .setPriority(Notification.PRIORITY_LOW)
        }

        return builder
            .setContentTitle(title.ifBlank { "Task Reminder" })
            .setContentText(details.ifBlank { "Task reminder active" })
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setCategory(Notification.CATEGORY_SERVICE)
                    setVisibility(Notification.VISIBILITY_SECRET)
                }
            }
            .build()
    }

    private fun onActivityReadyInternal() {
        Log.d("ReminderService", "Activity ready signal received for task ID $activeTaskId.")

        // Transition foreground notification to silent channel immediately
        // to prevent duplicate or heads-up banners over the visible dialogue
        val pendingIntent = currentFullScreenPendingIntent
        if (pendingIntent != null) {
            try {
                val silentNotification = buildSilentNotification(
                    activeTaskId,
                    currentTaskTitle,
                    currentTaskDetails,
                    pendingIntent
                )
                startForegroundServiceWithNotification(currentNotificationId, silentNotification)
                Log.d("ReminderService", "Transitioned foreground notification to silent channel for task ID $activeTaskId")
            } catch (e: Exception) {
                Log.w("ReminderService", "Could not transition notification to silent: ${e.message}")
            }
        }

        if (!taskSpeakEnabled) return
        Log.d("ReminderService", "Triggering synchronized speech for task ID $activeTaskId.")
        triggerSpeech()
    }

    private fun triggerSpeech() {
        if (isStopping) return
        mainHandler.removeCallbacks(fallbackRunnable)
        if (speechTriggered.compareAndSet(false, true)) {
            val text = speechTextToSpeak
            if (!text.isNullOrBlank()) {
                Log.d("ReminderService", "Starting TTS speak: $text")
                speaker?.speak(text)
            }
        } else {
            Log.d("ReminderService", "Speech already triggered; preventing duplicate speech.")
        }
    }

    private fun finishService() {
        if (isStopping) return
        isStopping = true

        mainHandler.removeCallbacks(fallbackRunnable)
        mainHandler.removeCallbacks(timeoutRunnable)

        isCurrentlySpeaking = false
        for (cb in listeners) {
            try {
                cb.onSpeechFinished()
            } catch (e: Exception) {
                Log.w("ReminderService", "Error in finishService callback: ${e.message}")
            }
        }

        try {
            speaker?.shutdown()
            speaker = null
        } catch (e: Exception) {
            Log.w("ReminderService", "Error during speaker shutdown: ${e.message}")
        }

        // Clean up foreground notification completely from drawer and status bar
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(currentNotificationId)
        } catch (e: Exception) {
            Log.w("ReminderService", "Error removing foreground notification: ${e.message}")
        }

        stopSelf()
    }

    override fun onDestroy() {
        finishService()
        if (activeService?.get() == this) {
            activeService = null
        }
        super.onDestroy()
    }
}
