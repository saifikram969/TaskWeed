package com.saif.scheduleapp.core.di

import com.saif.scheduleapp.core.platform.Haptics
import com.saif.scheduleapp.core.platform.WebHaptics
import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.platform.Speaker
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.reminder.WebReminderScheduler
import com.saif.scheduleapp.reminder.WebSpeaker
import org.koin.core.module.Module
import org.koin.dsl.module

class JsPdfReader : PdfReader {
    override val isSupported: Boolean = false

    override suspend fun extractText(bytes: ByteArray): String {
        return "PDF text extraction requires external pdf.js on JavaScript."
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { WebReminderScheduler() }
    single<Speaker> { WebSpeaker() }
    single<PdfReader> { JsPdfReader() }
    single<Haptics> { WebHaptics() }

    single<ScheduleRepository> { InMemoryScheduleRepository() }
}
