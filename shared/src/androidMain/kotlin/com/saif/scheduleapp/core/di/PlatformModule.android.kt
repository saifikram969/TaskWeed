package com.saif.scheduleapp.core.di

import android.util.Log
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidReminderScheduler : ReminderScheduler {
    override fun schedule(task: ScheduleTask) {
        Log.d("ScheduleApp", "AndroidReminderScheduler: Scheduled reminder for '${task.title}' at ${task.startMinutes} mins")
    }

    override fun cancel(taskId: Long) {
        Log.d("ScheduleApp", "AndroidReminderScheduler: Canceled reminder for task ID $taskId")
    }
}

class AndroidSpeaker : Speaker {
    override fun speak(text: String) {
        Log.d("ScheduleApp", "AndroidSpeaker: Speaking '$text'")
    }
}

class AndroidPdfReader : PdfReader {
    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires a third-party library on Android (e.g. PDFBox)."
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { AndroidReminderScheduler() }
    single<Speaker> { AndroidSpeaker() }
    single<PdfReader> { AndroidPdfReader() }
}
