package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.ClearAllTasks
import com.saif.scheduleapp.domain.usecase.DecideReminder
import com.saif.scheduleapp.domain.usecase.DeleteTask
import com.saif.scheduleapp.domain.usecase.GetMissedTasks
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
    factory { DecideReminder() }
    factory { GetTask(repository = get()) }
    factory { UpdateTask(repository = get(), scheduler = get(), decideReminder = get()) }
    factory { DeleteTask(repository = get(), scheduler = get(), decideReminder = get()) }
    factory { GetStats(repository = get()) }
    factory { GetMissedTasks(repository = get()) }

    // Presentation ViewModels
    single {
        HomeViewModel(
            getTodayTasks = get(),
            markTaskDone = get(),
            getStats = get(),
            getMissedTasks = get(),
            pdfReader = getOrNull()
        )
    }
    factory { params ->
        TaskFormViewModel(
            getTask = get(),
            addTask = get(),
            updateTask = get(),
            deleteTask = get(),
            taskId = params.getOrNull<Long>()
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
    single {
        StatsViewModel(
            getStats = get()
        )
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule, platformModule)
    }
