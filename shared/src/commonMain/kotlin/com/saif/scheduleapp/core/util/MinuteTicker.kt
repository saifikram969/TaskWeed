package com.saif.scheduleapp.core.util

import kotlin.time.Clock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone

/**
 * Emits the current minutes since midnight (0..1439) immediately upon subscription,
 * and subsequently emits at the beginning of every minute.
 */
fun minuteTicker(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System
): Flow<Int> = flow {
    emit(TimeUtils.currentMinutesSinceMidnight(timeZone, clock))
    while (currentCoroutineContext().isActive) {
        val nowMs = clock.now().toEpochMilliseconds()
        val millisUntilNextMinute = 60_000L - (nowMs % 60_000L)
        // Add a 50ms buffer to ensure crossing the minute boundary safely
        delay(millisUntilNextMinute + 50L)
        emit(TimeUtils.currentMinutesSinceMidnight(timeZone, clock))
    }
}
