package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import org.koin.core.module.Module
import org.koin.dsl.module

class WasmJsReminderScheduler : ReminderScheduler {
    override fun schedule(task: ScheduleTask) {
        println("WasmJsReminderScheduler: Scheduled reminder for '${task.title}' at ${task.startMinutes} mins")
    }

    override fun cancel(taskId: Long) {
        println("WasmJsReminderScheduler: Canceled reminder for task ID $taskId")
    }
}

class WasmJsSpeaker : Speaker {
    override fun speak(text: String) {
        println("WasmJsSpeaker: Speaking '$text'")
    }
}

class WasmJsPdfReader : PdfReader {
    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires external pdf.js on WebAssembly."
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { WasmJsReminderScheduler() }
    single<Speaker> { WasmJsSpeaker() }
    single<PdfReader> { WasmJsPdfReader() }
}
