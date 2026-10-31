package com.saif.scheduleapp.data.local

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.db.ScheduleTask as ScheduleTaskEntity

fun ScheduleTaskEntity.toDomain(): ScheduleTask {
    return ScheduleTask(
        id = id,
        title = title,
        details = details,
        startMinutes = startMinutes.toInt(),
        endMinutes = endMinutes.toInt(),
        category = category.toCategory(),
        isDone = isDone != 0L
    )
}

fun String.toCategory(): Category {
    return try {
        Category.valueOf(this)
    } catch (e: IllegalArgumentException) {
        Category.LEARNING
    }
}

fun Category.toDbString(): String = this.name
