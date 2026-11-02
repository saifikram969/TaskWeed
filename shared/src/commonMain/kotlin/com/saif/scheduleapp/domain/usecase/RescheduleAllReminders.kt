package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository

class RescheduleAllReminders(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke() {
        val tasks = repository.getAllTasks()
        tasks.forEach { task ->
            scheduler.schedule(task)
        }
    }
}
