package com.saif.scheduleapp.domain.model

import kotlinx.datetime.LocalDate

data class MissedTask(
    val task: ScheduleTask,
    val date: LocalDate
)

