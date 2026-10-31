package com.saif.scheduleapp.presentation.reminder

import com.saif.scheduleapp.domain.model.ScheduleTask

data class ReminderUiState(
    val task: ScheduleTask? = null,
    val isSpeaking: Boolean = false
)

