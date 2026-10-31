package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [ReminderScheduler] using Apple's [UNUserNotificationCenter].
 *
 * NOTE ON iOS CLOSED-APP REMINDERS:
 * When the app is closed, scheduled reminders trigger the iOS system notification banner
 * and play the default notification sound. iOS does not support background speech synthesis
 * when the app is suspended or terminated; speech is handled by [IosNotificationDelegate]
 * only while the app is open.
 */
class IosReminderScheduler(
    private val delegate: IosNotificationDelegate
) : ReminderScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    init {
        // Attach delegate to receive notifications while app is in foreground
        center.delegate = delegate
        requestAuthorization()
    }

    /**
     * Request notification authorization on first launch.
     */
    fun requestAuthorization() {
        val options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge

        center.requestAuthorizationWithOptions(options) { granted, error ->
            if (error != null) {
                println("IosReminderScheduler: Authorization error: $error")
            } else {
                println("IosReminderScheduler: Notification authorization granted: $granted")
            }
        }
    }

    override fun schedule(task: ScheduleTask) {
        val content = UNMutableNotificationContent().apply {
            setTitle(task.title.ifBlank { "Task Reminder" })
            if (task.details.isNotBlank()) {
                setBody(task.details)
            }
            setSound(UNNotificationSound.defaultSound())
        }

        // UNCalendarNotificationTrigger with hour and minute and repeats = true
        val hour = (task.startMinutes / 60).toLong()
        val minute = (task.startMinutes % 60).toLong()

        val dateComponents = NSDateComponents().apply {
            this.hour = hour
            this.minute = minute
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        // Use task ID as notification identifier
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = task.id.toString(),
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("IosReminderScheduler: Error scheduling notification for task ${task.id}: $error")
            } else {
                println("IosReminderScheduler: Scheduled notification for task ${task.id} at $hour:$minute (repeats=true)")
            }
        }
    }

    override fun cancel(taskId: Long) {
        val identifiers = listOf(taskId.toString(), "${taskId}_snooze")
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    override fun snooze(task: ScheduleTask, minutes: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Snoozed: ${task.title.ifBlank { "Task Reminder" }}")
            if (task.details.isNotBlank()) {
                setBody(task.details)
            }
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = (minutes * 60).toDouble().coerceAtLeast(1.0),
            repeats = false
        )

        val snoozeIdentifier = "${task.id}_snooze"
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = snoozeIdentifier,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("IosReminderScheduler: Error scheduling snooze for task ${task.id}: $error")
            } else {
                println("IosReminderScheduler: Snoozed task ${task.id} for $minutes min")
            }
        }
    }
}

