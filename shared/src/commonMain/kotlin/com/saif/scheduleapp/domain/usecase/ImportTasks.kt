package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DayOfWeek

class ImportTasks(
    private val addTask: AddTask,
    private val clearAllTasks: ClearAllTasks
) {
    suspend operator fun invoke(
        tasks: List<ScheduleTask>,
        replaceExisting: Boolean
    ): Result<Int> {
        if (replaceExisting) {
            clearAllTasks()
        }
        var count = 0
        for (task in tasks) {
            val taskToImport = task.copy(
                repeatDays = DayOfWeek.entries.toSet(),
                voiceEnabled = true
            )
            val result = addTask(taskToImport)
            if (result.isSuccess) {
                count++
            }
        }
        return Result.success(count)
    }
}
