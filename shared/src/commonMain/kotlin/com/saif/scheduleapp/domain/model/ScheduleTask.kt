package com.saif.scheduleapp.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

enum class Category {
    LEARNING,
    JOBS,
    PRAYER,
    REST
}

data class ScheduleTask(
    val id: Long,
    val title: String,
    val details: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val category: Category,
    val isDone: Boolean = false,
    val repeatDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val voiceEnabled: Boolean = true
) {
    fun isActiveOn(date: LocalDate): Boolean {
        return date.dayOfWeek in repeatDays
    }
}
