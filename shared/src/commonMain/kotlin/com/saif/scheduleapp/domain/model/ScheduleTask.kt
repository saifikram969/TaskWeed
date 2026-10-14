package com.saif.scheduleapp.domain.model

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
    val isDone: Boolean = false
)
