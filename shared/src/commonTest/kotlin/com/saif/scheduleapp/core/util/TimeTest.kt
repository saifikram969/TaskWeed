package com.saif.scheduleapp.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeTest {

    @Test
    fun testFormatMinutesToTimeString() {
        assertEquals("09:30 AM", TimeUtils.formatMinutesToTimeString(570))
        assertEquals("02:00 PM", TimeUtils.formatMinutesToTimeString(840))
        assertEquals("12:00 AM", TimeUtils.formatMinutesToTimeString(0))
        assertEquals("12:00 PM", TimeUtils.formatMinutesToTimeString(720))
        assertEquals("11:59 PM", TimeUtils.formatMinutesToTimeString(1439))
    }

    @Test
    fun testFormatTimeRange() {
        assertEquals(
            "09:30 AM - 11:00 AM",
            TimeUtils.formatTimeRange(570, 660)
        )
    }

    @Test
    fun testIsCurrent() {
        // start-inclusive, end-exclusive: 570 <= now < 660
        assertTrue(TimeUtils.isCurrent(startMinutes = 570, endMinutes = 660, currentMinutes = 570))
        assertTrue(TimeUtils.isCurrent(startMinutes = 570, endMinutes = 660, currentMinutes = 600))
        assertFalse(TimeUtils.isCurrent(startMinutes = 570, endMinutes = 660, currentMinutes = 660))
        assertFalse(TimeUtils.isCurrent(startMinutes = 570, endMinutes = 660, currentMinutes = 569))
    }

    @Test
    fun testIsUpcoming() {
        // strictly greater: startMinutes > currentMinutes
        assertTrue(TimeUtils.isUpcoming(startMinutes = 660, currentMinutes = 570))
        assertFalse(TimeUtils.isUpcoming(startMinutes = 570, currentMinutes = 570))
        assertFalse(TimeUtils.isUpcoming(startMinutes = 500, currentMinutes = 570))
    }

    @Test
    fun testCurrentMinutesRange() {
        val currentMinutes = TimeUtils.currentMinutesSinceMidnight()
        assertTrue(currentMinutes in 0..1439, "Current minutes must be within 0..1439")
    }
}
