package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first

class RescheduleAllReminders(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke() {
        val tasks = repository.observeTasks().first()
        tasks.filter { !it.isDone }.forEach { task ->
            scheduler.schedule(task)
        }
    }
}

