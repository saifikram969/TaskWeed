package com.saif.scheduleapp.core.di

import android.content.Context
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
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidPdfReader : PdfReader {
    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires a third-party library on Android (e.g. PDFBox)."
    }
}

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
