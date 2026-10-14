package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import com.saif.scheduleapp.presentation.home.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule: Module = module {
    // Include platform-specific bindings (ReminderScheduler, Speaker, PdfReader)
    includes(platformModule)

    // Repository
    single<ScheduleRepository> { InMemoryScheduleRepository() }

    // Use cases
    factory { GetTodayTasks(repository = get()) }
    factory { MarkTaskDone(repository = get()) }

    // Presentation ViewModel
    factory {
        HomeViewModel(
            getTodayTasks = get(),
            markTaskDone = get()
        )
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }
