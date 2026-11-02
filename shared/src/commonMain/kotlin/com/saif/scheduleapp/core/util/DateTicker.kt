package com.saif.scheduleapp.core.util

import kotlin.time.Clock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Emits today's date immediately, checks every minute, and emits again only when the date changes.
 */
fun dateTicker(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System
): Flow<LocalDate> = flow {
    var currentDate = TimeUtils.nowLocalDateTime(timeZone, clock).date
    emit(currentDate)
    while (currentCoroutineContext().isActive) {
        val nowMs = clock.now().toEpochMilliseconds()
        val millisUntilNextMinute = 60_000L - (nowMs % 60_000L)
        delay(millisUntilNextMinute + 50L)
        val newDate = TimeUtils.nowLocalDateTime(timeZone, clock).date
        if (newDate != currentDate) {
            currentDate = newDate
            emit(newDate)
        }
    }
}

