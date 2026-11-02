package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.data.local.DatabaseDriverFactory
import com.saif.scheduleapp.data.repository.SqlDelightScheduleRepository
import com.saif.scheduleapp.db.ScheduleDatabase
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.platform.IosPdfReader
import com.saif.scheduleapp.reminder.IosNotificationDelegate
import com.saif.scheduleapp.reminder.IosReminderScheduler
import com.saif.scheduleapp.reminder.IosSpeaker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Speaker> { IosSpeaker() }
    single { IosNotificationDelegate(speaker = get()) }
    single<ReminderScheduler> { IosReminderScheduler(delegate = get()) }
    single<PdfReader> { IosPdfReader() }

    single { DatabaseDriverFactory() }
    single { ScheduleDatabase(driver = get<DatabaseDriverFactory>().create()) }
    single<ScheduleRepository> { SqlDelightScheduleRepository(database = get()) }
}
