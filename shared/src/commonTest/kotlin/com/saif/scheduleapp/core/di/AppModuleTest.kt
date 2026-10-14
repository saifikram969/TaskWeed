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
            modules(appModule)
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
    }
}
