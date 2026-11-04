package com.saif.scheduleapp.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.DecideReminder
import com.saif.scheduleapp.domain.usecase.TriggerDecision
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

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
        if (taskId <= 0) {
            Log.w("ReminderReceiver", "Received trigger with invalid task ID: $taskId; ignoring.")
            return
        }

        Log.d("ReminderReceiver", "Alarm fired for task ID $taskId. Invoking goAsync().")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val koin = KoinPlatform.getKoinOrNull()
                val repository = koin?.getOrNull<ScheduleRepository>()
                val decideReminder = koin?.getOrNull<DecideReminder>() ?: DecideReminder()
                val scheduler = koin?.getOrNull<ReminderScheduler>() ?: AndroidReminderScheduler(context)

                if (repository == null) {
                    Log.e("ReminderReceiver", "ScheduleRepository unavailable from Koin.")
                    return@launch
                }

                // Look up task
                val task = repository.getTask(taskId)
                if (task == null) {
                    Log.d("ReminderReceiver", "Task ID $taskId not found or deleted; skipping notification and rescheduling.")
                    return@launch
                }

                val today = TimeUtils.nowLocalDateTime().date
                val isDoneToday = repository.observeTasks(today)
                    .first()
                    .find { it.id == taskId }
                    ?.isDone ?: false

                // Decide whether to notify or skip
                when (val decision = decideReminder.decideOnTrigger(task, today, isDoneToday)) {
                    is TriggerDecision.Notify -> {
                        Log.d("ReminderReceiver", "Decision: Notify (speak=${decision.speak}) for task ID $taskId")
                        startReminderService(context, decision.task, decision.speak)
                    }
                    is TriggerDecision.Skip -> {
                        Log.d("ReminderReceiver", "Decision: Skip for task ID $taskId (inactive today or already completed)")
                    }
                }

                // Schedule next occurrence after processing, unless the task was deleted
                scheduler.schedule(task)
                Log.d("ReminderReceiver", "Rescheduled next occurrence for task ID $taskId")
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error processing reminder for task ID $taskId: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startReminderService(context: Context, task: ScheduleTask, speak: Boolean) {
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra(ReminderService.EXTRA_TASK_ID, task.id)
            putExtra(ReminderService.EXTRA_TASK_TITLE, task.title)
            putExtra(ReminderService.EXTRA_TASK_DETAILS, task.details)
            putExtra(ReminderService.EXTRA_TASK_START_MINUTES, task.startMinutes)
            putExtra(ReminderService.EXTRA_TASK_END_MINUTES, task.endMinutes)
            putExtra(ReminderService.EXTRA_TASK_CATEGORY, task.category.name)
            putExtra(ReminderService.EXTRA_SPEAK, speak)
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
    }
}
