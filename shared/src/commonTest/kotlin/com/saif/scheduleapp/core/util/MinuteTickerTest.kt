package com.saif.scheduleapp.core.util

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MinuteTickerTest {

    @Test
    fun testMinuteTickerEmitsCurrentMinuteImmediately() = runTest {
        val minute = minuteTicker().first()
        assertTrue(minute in 0..1439, "Expected minute between 0 and 1439, got $minute")
    }
}
