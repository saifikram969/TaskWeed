package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.core.platform.Haptics
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.ClearAllTasks
import com.saif.scheduleapp.domain.usecase.DecideReminder
import com.saif.scheduleapp.domain.usecase.DeleteTask
import com.saif.scheduleapp.domain.usecase.GetStats
import com.saif.scheduleapp.domain.usecase.GetTask
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.ImportTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import com.saif.scheduleapp.domain.usecase.RescheduleAllReminders
import com.saif.scheduleapp.domain.usecase.UpdateTask
import com.saif.scheduleapp.presentation.home.HomeViewModel
import com.saif.scheduleapp.presentation.importschedule.ImportViewModel
import com.saif.scheduleapp.presentation.reminder.ReminderViewModel
import com.saif.scheduleapp.presentation.stats.StatsViewModel
import com.saif.scheduleapp.presentation.taskform.TaskFormViewModel
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {

    @Test
    fun testAppModuleDependenciesResolution() {
        val fakeRepo = object : ScheduleRepository {
            override fun observeTasks(date: kotlinx.datetime.LocalDate) = kotlinx.coroutines.flow.flowOf(emptyList<com.saif.scheduleapp.domain.model.ScheduleTask>())
            override suspend fun setDone(taskId: Long, date: kotlinx.datetime.LocalDate, done: Boolean) {}
            override suspend fun addTask(task: com.saif.scheduleapp.domain.model.ScheduleTask): Long = 1L
            override suspend fun clearAllTasks() {}
            override suspend fun getAllTasks() = emptyList<com.saif.scheduleapp.domain.model.ScheduleTask>()
        }
        val fakeScheduler = object : ReminderScheduler {
            override fun schedule(task: com.saif.scheduleapp.domain.model.ScheduleTask) {}
            override fun cancel(taskId: Long) {}
            override fun snooze(task: com.saif.scheduleapp.domain.model.ScheduleTask, minutes: Int) {}
        }
        val fakeSpeaker = object : Speaker {
            override fun speak(text: String) {}
        }
        val fakePdfReader = object : PdfReader {
            override val isSupported: Boolean = true
            override suspend fun extractText(bytes: ByteArray): String = "09:00 AM - 10:00 AM Meeting"
        }
        val fakeHaptics = object : Haptics {
            override fun tick() {}
        }

        val testModule = org.koin.dsl.module {
            single<ScheduleRepository> { fakeRepo }
            single<ReminderScheduler> { fakeScheduler }
            single<Speaker> { fakeSpeaker }
            single<PdfReader> { fakePdfReader }
            single<Haptics> { fakeHaptics }
        }

        val koinApp = koinApplication {
            modules(appModule, testModule)
        }

        val getTodayTasks = koinApp.koin.get<GetTodayTasks>()
        assertNotNull(getTodayTasks)

        val markTaskDone = koinApp.koin.get<MarkTaskDone>()
        assertNotNull(markTaskDone)

        val addTask = koinApp.koin.get<AddTask>()
        assertNotNull(addTask)

        val getTask = koinApp.koin.get<GetTask>()
        assertNotNull(getTask)

        val updateTask = koinApp.koin.get<UpdateTask>()
        assertNotNull(updateTask)

        val deleteTask = koinApp.koin.get<DeleteTask>()
        assertNotNull(deleteTask)

        val decideReminder = koinApp.koin.get<DecideReminder>()
        assertNotNull(decideReminder)

        val getStats = koinApp.koin.get<GetStats>()
        assertNotNull(getStats)

        val haptics = koinApp.koin.get<Haptics>()
        assertNotNull(haptics)

        val rescheduleAllReminders = koinApp.koin.get<RescheduleAllReminders>()
        assertNotNull(rescheduleAllReminders)

        val reminderViewModel = koinApp.koin.get<ReminderViewModel>()
        assertNotNull(reminderViewModel)

        val clearAllTasks = koinApp.koin.get<ClearAllTasks>()
        assertNotNull(clearAllTasks)

        val importTasks = koinApp.koin.get<ImportTasks>()
        assertNotNull(importTasks)

        val importViewModel = koinApp.koin.get<ImportViewModel>()
        assertNotNull(importViewModel)

        val homeViewModel = koinApp.koin.get<HomeViewModel>()
        assertNotNull(homeViewModel)

        val statsViewModel = koinApp.koin.get<StatsViewModel>()
        assertNotNull(statsViewModel)
    }
}
