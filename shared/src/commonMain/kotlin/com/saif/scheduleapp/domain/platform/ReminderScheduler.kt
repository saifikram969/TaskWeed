package com.saif.scheduleapp.domain.platform

import com.saif.scheduleapp.domain.model.ScheduleTask

interface ReminderScheduler {
    fun schedule(task: ScheduleTask)
    fun cancel(taskId: Long)
}
