package com.saif.scheduleapp.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.util.nextTriggerMillis
import kotlinx.datetime.TimeZone

class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(task: ScheduleTask) {
        if (alarmManager == null) {
            Log.e("AndroidReminderScheduler", "AlarmManager service not available.")
            return
        }

        val now = TimeUtils.nowLocalDateTime()
        val zone = TimeZone.currentSystemDefault()
        val triggerMillis = nextTriggerMillis(
            startMinutes = task.startMinutes,
            now = now,
            zone = zone
        )

        val pendingIntent = createPendingIntent(task)
        scheduleAlarm(triggerMillis, pendingIntent, task.id, "schedule")
    }

    override fun snooze(task: ScheduleTask, minutes: Int) {
        if (alarmManager == null) {
            Log.e("AndroidReminderScheduler", "AlarmManager service not available.")
            return
        }

        val triggerMillis = java.lang.System.currentTimeMillis() + (minutes * 60 * 1000L)
        val pendingIntent = createPendingIntent(task)
        scheduleAlarm(triggerMillis, pendingIntent, task.id, "snooze ($minutes min)")
    }

    override fun cancel(taskId: Long) {
        if (alarmManager == null) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("AndroidReminderScheduler", "Canceled alarm for task ID $taskId")
    }

    private fun scheduleAlarm(
        triggerMillis: Long,
        pendingIntent: PendingIntent,
        taskId: Long,
        operation: String
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    Log.w(
                        "AndroidReminderScheduler",
                        "SCHEDULE_EXACT_ALARM permission not held. Using setAndAllowWhileIdle for task ID $taskId."
                    )
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                    return
                }
            }

            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
            Log.d("AndroidReminderScheduler", "Set exact alarm for task ID $taskId at epoch $triggerMillis ($operation)")
        } catch (e: SecurityException) {
            Log.w("AndroidReminderScheduler", "SecurityException scheduling exact alarm: ${e.message}. Falling back.")
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun createPendingIntent(task: ScheduleTask): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(ReminderReceiver.EXTRA_TASK_DETAILS, task.details)
            putExtra(ReminderReceiver.EXTRA_TASK_START_MINUTES, task.startMinutes)
            putExtra(ReminderReceiver.EXTRA_TASK_END_MINUTES, task.endMinutes)
            putExtra(ReminderReceiver.EXTRA_TASK_CATEGORY, task.category.name)
        }

        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
