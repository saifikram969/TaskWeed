package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask

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
            val result = addTask(task)
            if (result.isSuccess) {
                count++
            }
        }
        return Result.success(count)
    }
}

