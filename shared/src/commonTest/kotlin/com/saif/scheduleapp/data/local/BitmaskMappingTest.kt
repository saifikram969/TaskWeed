package com.saif.scheduleapp.data.local

import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

class BitmaskMappingTest {

    @Test
    fun testAllSevenDaysIs127() {
        val allDays = DayOfWeek.entries.toSet()
        val bitmask = allDays.toBitmask()
        assertEquals(127, bitmask)

        val restored = 127.toDayOfWeekSet()
        assertEquals(allDays, restored)
    }

    @Test
    fun testSingleDays() {
        // Monday -> bit 0 -> 1
        assertEquals(1, setOf(DayOfWeek.MONDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.MONDAY), 1.toDayOfWeekSet())

        // Tuesday -> bit 1 -> 2
        assertEquals(2, setOf(DayOfWeek.TUESDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.TUESDAY), 2.toDayOfWeekSet())

        // Wednesday -> bit 2 -> 4
        assertEquals(4, setOf(DayOfWeek.WEDNESDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.WEDNESDAY), 4.toDayOfWeekSet())

        // Thursday -> bit 3 -> 8
        assertEquals(8, setOf(DayOfWeek.THURSDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.THURSDAY), 8.toDayOfWeekSet())

        // Friday -> bit 4 -> 16
        assertEquals(16, setOf(DayOfWeek.FRIDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.FRIDAY), 16.toDayOfWeekSet())

        // Saturday -> bit 5 -> 32
        assertEquals(32, setOf(DayOfWeek.SATURDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.SATURDAY), 32.toDayOfWeekSet())

        // Sunday -> bit 6 -> 64
        assertEquals(64, setOf(DayOfWeek.SUNDAY).toBitmask())
        assertEquals(setOf(DayOfWeek.SUNDAY), 64.toDayOfWeekSet())
    }

    @Test
    fun testSubsets() {
        // Weekdays: Mon(1) + Tue(2) + Wed(4) + Thu(8) + Fri(16) = 31
        val weekdays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        assertEquals(31, weekdays.toBitmask())
        assertEquals(weekdays, 31.toDayOfWeekSet())

        // Weekends: Sat(32) + Sun(64) = 96
        val weekends = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        assertEquals(96, weekends.toBitmask())
        assertEquals(weekends, 96.toDayOfWeekSet())

        // Mon + Wed + Fri = 1 + 4 + 16 = 21
        val mwf = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        assertEquals(21, mwf.toBitmask())
        assertEquals(mwf, 21.toDayOfWeekSet())
    }

    @Test
    fun testEmptySetIsZero() {
        val empty = emptySet<DayOfWeek>()
        assertEquals(0, empty.toBitmask())
        assertEquals(empty, 0.toDayOfWeekSet())
    }

    @Test
    fun testLongConversion() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        val bitmaskLong = days.toBitmaskLong()
        assertEquals(3L, bitmaskLong)
        assertEquals(days, 3L.toDayOfWeekSet())
    }
}

