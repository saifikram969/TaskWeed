package com.example.taskweed

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.core.di.initKoin
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.presentation.reminder.ReminderScreen
import com.saif.scheduleapp.presentation.reminder.ReminderViewModel
import com.saif.scheduleapp.reminder.ReminderService
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.mp.KoinPlatform

class ReminderActivity : ComponentActivity() {

    private var currentTaskId: Long = -1L
    private var reminderViewModel: ReminderViewModel? = null
    private var hasNotifiedReady: Boolean = false

    private val speechCallback = object : ReminderService.SpeechCallback {
        override fun onSpeechStarted() {
            reminderViewModel?.setSpeaking(true)
        }

        override fun onSpeechFinished() {
            reminderViewModel?.setSpeaking(false)
        }

        override fun onSpeechError(errorCode: Int?) {
            reminderViewModel?.setSpeaking(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Enable show when locked and turn screen on for alarm full-screen intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Ensure Koin is ready
        if (KoinPlatform.getKoinOrNull() == null) {
            initKoin {
                androidContext(applicationContext)
            }
        }

        // Read task ID and task info from intent extras
        val taskId = intent.getLongExtra(ReminderService.EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(ReminderService.EXTRA_TASK_TITLE).orEmpty()
        val details = intent.getStringExtra(ReminderService.EXTRA_TASK_DETAILS).orEmpty()
        val startMinutes = intent.getIntExtra(ReminderService.EXTRA_TASK_START_MINUTES, 0)
        val endMinutes = intent.getIntExtra(ReminderService.EXTRA_TASK_END_MINUTES, 0)
        val speak = intent.getBooleanExtra(ReminderService.EXTRA_SPEAK, true)
        val categoryName = intent.getStringExtra(ReminderService.EXTRA_TASK_CATEGORY) ?: Category.LEARNING.name
        val category = try {
            Category.valueOf(categoryName)
        } catch (_: Exception) {
            Category.LEARNING
        }

        currentTaskId = taskId
        Log.d("ReminderActivity", "onCreate: taskId=$taskId, title='$title', speak=$speak")

        val vm: ReminderViewModel = get()
        reminderViewModel = vm

        if (taskId > 0L) {
            val taskFromExtras = ScheduleTask(
                id = taskId,
                title = title,
                details = details,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                category = category,
                voiceEnabled = speak
            )
            // Disable synthetic countdown; real TTS lifecycle drives sound-wave animation
            vm.setTask(taskFromExtras, triggerCountdown = false)
            vm.loadTask(taskId, triggerCountdown = false)
            // Immediately reflect if speech is already active from service
            vm.setSpeaking(ReminderService.isSpeaking(taskId))
        }

        // Register speech lifecycle callbacks with ReminderService
        ReminderService.registerSpeechCallback(taskId, speechCallback)

        setContent {
            ScheduleTheme {
                val state by vm.uiState.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
                val isVisible = lifecycleState.isAtLeast(Lifecycle.State.STARTED)

                ReminderScreen(
                    state = state,
                    isScreenVisible = isVisible,
                    onStartNow = {
                        Log.d("ReminderActivity", "onStartNow triggered - stopping speech and finishing activity")
                        ReminderService.stopSpeechAndFinish(taskId)
                        vm.onStartNow {
                            finish()
                        }
                    },
                    onSnooze = {
                        Log.d("ReminderActivity", "onSnooze triggered - stopping speech and snoozing")
                        ReminderService.stopSpeechAndFinish(taskId)
                        vm.onSnooze(minutes = 5) {
                            finish()
                        }
                    }
                )
            }
        }
    }

    private fun notifyActivityReadyIfVisible() {
        if (!hasNotifiedReady && currentTaskId > 0L) {
            hasNotifiedReady = true
            Log.d("ReminderActivity", "Window is visible and focused; signalling ReminderService readiness for task $currentTaskId")
            ReminderService.onActivityReady(currentTaskId)
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.post {
            notifyActivityReadyIfVisible()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            notifyActivityReadyIfVisible()
        }
    }

    override fun onDestroy() {
        ReminderService.unregisterSpeechCallback(speechCallback)
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notificationId = 10000 + (currentTaskId % 10000).toInt()
            notificationManager?.cancel(notificationId)
        } catch (e: Exception) {
            Log.w("ReminderActivity", "Error cancelling notification in onDestroy: ${e.message}")
        }
        super.onDestroy()
        Log.d("ReminderActivity", "onDestroy called")
    }
}
