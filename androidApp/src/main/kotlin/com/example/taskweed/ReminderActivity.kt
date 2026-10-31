package com.example.taskweed

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.saif.scheduleapp.core.designsystem.ScheduleAppTheme
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
        val categoryName = intent.getStringExtra(ReminderService.EXTRA_TASK_CATEGORY) ?: Category.LEARNING.name
        val category = try {
            Category.valueOf(categoryName)
        } catch (_: Exception) {
            Category.LEARNING
        }

        Log.d("ReminderActivity", "onCreate: taskId=$taskId, title='$title'")

        val viewModel: ReminderViewModel = get()

        if (taskId > 0L) {
            val taskFromExtras = ScheduleTask(
                id = taskId,
                title = title,
                details = details,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                category = category
            )
            viewModel.setTask(taskFromExtras)
            viewModel.loadTask(taskId)
        }

        setContent {
            ScheduleAppTheme {
                val state by viewModel.uiState.collectAsState()
                ReminderScreen(
                    state = state,
                    onStartNow = {
                        Log.d("ReminderActivity", "onStartNow triggered - finishing activity")
                        viewModel.onStartNow {
                            finish()
                        }
                    },
                    onSnooze = {
                        Log.d("ReminderActivity", "onSnooze triggered - finishing activity")
                        viewModel.onSnooze(minutes = 5) {
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ReminderActivity", "onDestroy called")
    }
}

