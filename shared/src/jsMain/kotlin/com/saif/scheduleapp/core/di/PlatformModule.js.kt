package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import org.koin.core.module.Module
import org.koin.dsl.module

class JsReminderScheduler : ReminderScheduler {
    override fun schedule(task: ScheduleTask) {
        println("JsReminderScheduler: Scheduled reminder for '${task.title}' at ${task.startMinutes} mins")
    }

    override fun cancel(taskId: Long) {
        println("JsReminderScheduler: Canceled reminder for task ID $taskId")
    }
}

class JsSpeaker : Speaker {
    override fun speak(text: String) {
        println("JsSpeaker: Speaking '$text'")
    }
}

class JsPdfReader : PdfReader {
    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires external pdf.js on JavaScript."
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { JsReminderScheduler() }
    single<Speaker> { JsSpeaker() }
    single<PdfReader> { JsPdfReader() }
}
