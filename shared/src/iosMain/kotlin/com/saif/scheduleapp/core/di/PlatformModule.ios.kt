package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import org.koin.core.module.Module
import org.koin.dsl.module

class IosReminderScheduler : ReminderScheduler {
    override fun schedule(task: ScheduleTask) {
        println("IosReminderScheduler: Scheduled reminder for '${task.title}' at ${task.startMinutes} mins")
    }

    override fun cancel(taskId: Long) {
        println("IosReminderScheduler: Canceled reminder for task ID $taskId")
    }
}

class IosSpeaker : Speaker {
    override fun speak(text: String) {
        println("IosSpeaker: Speaking '$text'")
    }
}

class IosPdfReader : PdfReader {
    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires PDFKit on iOS."
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { IosReminderScheduler() }
    single<Speaker> { IosSpeaker() }
    single<PdfReader> { IosPdfReader() }
}
