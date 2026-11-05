package com.saif.scheduleapp.core.di

import android.content.Context
import com.saif.scheduleapp.core.platform.AndroidHaptics
import com.saif.scheduleapp.core.platform.Haptics
import com.saif.scheduleapp.data.local.DatabaseDriverFactory
import com.saif.scheduleapp.data.repository.SqlDelightScheduleRepository
import com.saif.scheduleapp.db.ScheduleDatabase
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.reminder.AndroidReminderScheduler
import com.saif.scheduleapp.reminder.AndroidSpeaker
import com.saif.scheduleapp.platform.AndroidPdfReader
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ReminderScheduler> {
        val context = getOrNull<Context>()
        if (context != null) {
            AndroidReminderScheduler(context = context)
        } else {
            object : ReminderScheduler {
                override fun schedule(task: ScheduleTask) {}
                override fun cancel(taskId: Long) {}
                override fun snooze(task: ScheduleTask, minutes: Int) {}
            }
        }
    }

    single<Speaker> {
        val context = getOrNull<Context>()
        if (context != null) {
            AndroidSpeaker(context = context)
        } else {
            object : Speaker {
                override fun speak(text: String) {}
            }
        }
    }

    single<PdfReader> { AndroidPdfReader() }
   // Do we really need this fallback, or should Context always be provided through Koin?
    single<Haptics> {
        val context = getOrNull<Context>()
        if (context != null) {
            AndroidHaptics(context = context)
        } else {
            object : Haptics {
                override fun tick() {}
            }
        }
    }

    single {
        val context = getOrNull<Context>()
            ?: throw IllegalStateException("Android Context must be provided in Koin to initialize DatabaseDriverFactory.")
        DatabaseDriverFactory(context = context)
    }

    single { ScheduleDatabase(driver = get<DatabaseDriverFactory>().create()) }

    single<ScheduleRepository> {
        SqlDelightScheduleRepository(database = get())
    }
}
