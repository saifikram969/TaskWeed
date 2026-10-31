package com.saif.scheduleapp.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.saif.scheduleapp.reminder.TRIGGER_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DETAILS = "extra_task_details"
        const val EXTRA_TASK_START_MINUTES = "extra_task_start_minutes"
        const val EXTRA_TASK_END_MINUTES = "extra_task_end_minutes"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
        val details = intent.getStringExtra(EXTRA_TASK_DETAILS) ?: ""
        val startMinutes = intent.getIntExtra(EXTRA_TASK_START_MINUTES, 0)
        val endMinutes = intent.getIntExtra(EXTRA_TASK_END_MINUTES, 0)
        val categoryName = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: Category.LEARNING.name
        val category = try {
            Category.valueOf(categoryName)
        } catch (_: Exception) {
            Category.LEARNING
        }

        Log.d("ReminderReceiver", "Alarm fired for task ID $taskId: '$title'")

        // 1. Start foreground ReminderService to post high-priority notification and speak
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra(ReminderService.EXTRA_TASK_ID, taskId)
            putExtra(ReminderService.EXTRA_TASK_TITLE, title)
            putExtra(ReminderService.EXTRA_TASK_DETAILS, details)
            putExtra(ReminderService.EXTRA_TASK_START_MINUTES, startMinutes)
            putExtra(ReminderService.EXTRA_TASK_END_MINUTES, endMinutes)
            putExtra(ReminderService.EXTRA_TASK_CATEGORY, category.name)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Failed to start ReminderService: ${e.message}", e)
        }

        // 2. Schedule the same task for the next day
        if (taskId > 0) {
            val task = ScheduleTask(
                id = taskId,
                title = title,
                details = details,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                category = category
            )
            val scheduler = AndroidReminderScheduler(context)
            scheduler.schedule(task)
            Log.d("ReminderReceiver", "Rescheduled task ID $taskId for next day.")
        }
    }
}

