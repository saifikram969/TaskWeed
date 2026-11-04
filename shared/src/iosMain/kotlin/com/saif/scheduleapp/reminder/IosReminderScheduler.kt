package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import kotlinx.datetime.DayOfWeek
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
 * and play the notification sound (if voiceEnabled is true). iOS does not support background
 * speech synthesis when the app is suspended or terminated; speech is handled by [IosNotificationDelegate]
 * only while the app is open.
 */
class IosReminderScheduler(
    private val delegate: IosNotificationDelegate
) : ReminderScheduler {

    companion object {
        /**
         * iOS System Pending Notification Limit:
         * Apple's UNUserNotificationCenter enforces a strict limit of approximately 64 pending
         * (scheduled) local notification requests per application. Any requests scheduled beyond
         * this limit are silently ignored or discarded by the iOS operating system.
         *
         * To ensure reliable scheduling:
         * 1. A task with all 7 repeat days uses a single daily repeating request instead of 7 requests.
         * 2. When tasks require individual weekday requests, requests are scheduled in task ID order
         *    up to the 64-request limit.
         * 3. Any requests exceeding the limit are dropped with an explicit warning logged.
         */
        const val MAX_PENDING_NOTIFICATION_REQUESTS = 64
    }

    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val scheduledTasks = mutableMapOf<Long, ScheduleTask>()

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
        if (task.repeatDays.isEmpty()) {
            cancel(task.id)
            return
        }

        // Cancel existing notification requests for this task before rebuilding
        cancel(task.id)
        scheduledTasks[task.id] = task

        rebuildScheduledNotifications()
    }

    override fun cancel(taskId: Long) {
        scheduledTasks.remove(taskId)

        // Cancel the plain identifier, all seven weekday identifiers, and snooze
        val identifiers = buildList {
            add(taskId.toString())
            for (weekday in 1..7) {
                add("${taskId}_$weekday")
            }
            add("${taskId}_snooze")
        }

        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
        println("IosReminderScheduler: Canceled notification requests for task $taskId (plain, all 7 weekdays, and snooze)")
    }

    override fun snooze(task: ScheduleTask, minutes: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Snoozed: ${task.title.ifBlank { "Task Reminder" }}")
            if (task.details.isNotBlank()) {
                setBody(task.details)
            }
            if (task.voiceEnabled) {
                setSound(UNNotificationSound.defaultSound())
            } else {
                setSound(null)
            }
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

    private fun rebuildScheduledNotifications() {
        val hour = { task: ScheduleTask -> (task.startMinutes / 60).toLong() }
        val minute = { task: ScheduleTask -> (task.startMinutes % 60).toLong() }

        // Generate all notification requests in task ID order
        val allRequests = mutableListOf<UNNotificationRequest>()
        val tasksInIdOrder = scheduledTasks.values.sortedBy { it.id }

        for (task in tasksInIdOrder) {
            val content = UNMutableNotificationContent().apply {
                setTitle(task.title.ifBlank { "Task Reminder" })
                if (task.details.isNotBlank()) {
                    setBody(task.details)
                }
                if (task.voiceEnabled) {
                    setSound(UNNotificationSound.defaultSound())
                } else {
                    setSound(null)
                }
            }

            val taskHour = hour(task)
            val taskMinute = minute(task)

            // When all 7 days are selected, use one repeating request without weekday
            if (task.repeatDays.size == 7) {
                val dateComponents = NSDateComponents().apply {
                    this.hour = taskHour
                    this.minute = taskMinute
                }
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    dateComponents = dateComponents,
                    repeats = true
                )
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = task.id.toString(),
                    content = content,
                    trigger = trigger
                )
                allRequests.add(request)
            } else {
                // Otherwise create one repeating request per selected weekday in weekday order
                val sortedDays = task.repeatDays.sortedBy { it.toIosWeekday() }
                for (day in sortedDays) {
                    val iosWeekday = day.toIosWeekday()
                    val dateComponents = NSDateComponents().apply {
                        this.weekday = iosWeekday
                        this.hour = taskHour
                        this.minute = taskMinute
                    }
                    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                        dateComponents = dateComponents,
                        repeats = true
                    )
                    val request = UNNotificationRequest.requestWithIdentifier(
                        identifier = "${task.id}_$iosWeekday",
                        content = content,
                        trigger = trigger
                    )
                    allRequests.add(request)
                }
            }
        }

        // Respect the iOS limit of approximately 64 pending requests
        val requestsToSchedule = allRequests.take(MAX_PENDING_NOTIFICATION_REQUESTS)
        val droppedRequests = allRequests.drop(MAX_PENDING_NOTIFICATION_REQUESTS)

        for (dropped in droppedRequests) {
            println("IosReminderScheduler: WARNING - iOS pending notification limit ($MAX_PENDING_NOTIFICATION_REQUESTS) exceeded. Dropping request ${dropped.identifier}.")
        }

        for (request in requestsToSchedule) {
            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("IosReminderScheduler: Error adding request ${request.identifier}: $error")
                } else {
                    println("IosReminderScheduler: Successfully scheduled request ${request.identifier}")
                }
            }
        }
    }

    /**
     * Maps Kotlin [DayOfWeek] to iOS [NSDateComponents.weekday] numbering:
     * Sunday = 1, Monday = 2, Tuesday = 3, Wednesday = 4, Thursday = 5, Friday = 6, Saturday = 7.
     */
    private fun DayOfWeek.toIosWeekday(): Long = when (this) {
        DayOfWeek.SUNDAY -> 1L
        DayOfWeek.MONDAY -> 2L
        DayOfWeek.TUESDAY -> 3L
        DayOfWeek.WEDNESDAY -> 4L
        DayOfWeek.THURSDAY -> 5L
        DayOfWeek.FRIDAY -> 6L
        DayOfWeek.SATURDAY -> 7L
    }
}
