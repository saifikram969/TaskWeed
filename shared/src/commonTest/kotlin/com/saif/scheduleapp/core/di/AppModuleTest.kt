package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import com.saif.scheduleapp.presentation.home.HomeViewModel
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {

    @Test
    fun testAppModuleDependenciesResolution() {
        val koinApp = koinApplication {
            modules(
                appModule,
                org.koin.dsl.module {
                    single<ScheduleRepository> { com.saif.scheduleapp.data.repository.InMemoryScheduleRepository() }
                }
            )
        }

        val repository = koinApp.koin.get<ScheduleRepository>()
        assertNotNull(repository)

        val getTodayTasks = koinApp.koin.get<GetTodayTasks>()
        assertNotNull(getTodayTasks)

        val markTaskDone = koinApp.koin.get<MarkTaskDone>()
        assertNotNull(markTaskDone)

        val reminderScheduler = koinApp.koin.get<ReminderScheduler>()
        assertNotNull(reminderScheduler)

        val speaker = koinApp.koin.get<Speaker>()
        assertNotNull(speaker)

        val pdfReader = koinApp.koin.get<PdfReader>()
        assertNotNull(pdfReader)

        val addTask = koinApp.koin.get<com.saif.scheduleapp.domain.usecase.AddTask>()
        assertNotNull(addTask)

        val addTaskViewModel = koinApp.koin.get<com.saif.scheduleapp.presentation.addtask.AddTaskViewModel>()
        assertNotNull(addTaskViewModel)

        val rescheduleAllReminders = koinApp.koin.get<com.saif.scheduleapp.domain.usecase.RescheduleAllReminders>()
        assertNotNull(rescheduleAllReminders)

        val reminderViewModel = koinApp.koin.get<com.saif.scheduleapp.presentation.reminder.ReminderViewModel>()
        assertNotNull(reminderViewModel)
    }
}
