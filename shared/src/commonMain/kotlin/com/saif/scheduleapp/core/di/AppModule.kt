package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.ClearAllTasks
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.ImportTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import com.saif.scheduleapp.domain.usecase.RescheduleAllReminders
import com.saif.scheduleapp.presentation.addtask.AddTaskViewModel
import com.saif.scheduleapp.presentation.home.HomeViewModel
import com.saif.scheduleapp.presentation.importschedule.ImportViewModel
import com.saif.scheduleapp.presentation.reminder.ReminderViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule: Module = module {
    // Include platform-specific bindings (ReminderScheduler, Speaker, PdfReader, ScheduleRepository)
    includes(platformModule)

    // Use cases
    factory { GetTodayTasks(repository = get()) }
    factory { MarkTaskDone(repository = get()) }
    factory { AddTask(repository = get(), scheduler = get()) }
    factory { RescheduleAllReminders(repository = get(), scheduler = get()) }
    factory { ClearAllTasks(repository = get(), scheduler = get()) }
    factory { ImportTasks(addTask = get(), clearAllTasks = get()) }

    // Presentation ViewModels
    factory {
        HomeViewModel(
            getTodayTasks = get(),
            markTaskDone = get(),
            pdfReader = getOrNull()
        )
    }
    factory {
        AddTaskViewModel(
            addTask = get()
        )
    }
    factory {
        ReminderViewModel(
            repository = get(),
            scheduler = get()
        )
    }
    factory {
        ImportViewModel(
            pdfReader = get(),
            importTasks = get()
        )
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule, platformModule)
    }
