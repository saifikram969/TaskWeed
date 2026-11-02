package com.saif.scheduleapp.domain.parser

import com.saif.scheduleapp.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParseScheduleTest {

    @Test
    fun testComplete19TaskSampleSchedule() {
        val scheduleText = """
            ========================================
            TaskWeed Daily Routine & Schedule
            Target Date: 2026-11-01
            ========================================
            
            5:00 - 5:30 AM Fajr Prayer & Morning Reflection
            5:30 - 6:30 Morning Gym & Workout
            6:30 - 7:15 Breakfast & Coffee
            7:15 – 8:45 DSA practice & Algorithms
            9:00 - 10:30 Kotlin Study & Deep Dive
            10:30 - 11:30 Office Team Sync & Standup
            11:30 - 12:30 Client Meeting
            12:30 - 1:30 PM Dhuhr Prayer & Lunch Break
            1:30 - 3:00 Architecture & PR-Review
            3:00 — 4:30 Work on Bug-fixing Ticket
            4:45 - 5:15 PM Asr Prayer Break
            5:15 - 6:00 Evening Walk & Relax
            6:00 - 7:15 Personal Project Research
            7:15 - 8:00 Maghrib Prayer & Dinner
            8:00 - 9:00 Compose Multiplatform Course
            9:00 - 9:45 PM Isha Prayer & Recitation
            9:45 - 10:30 Book Reading & Journaling
            10:30 - 11:15 Night Wind Down & Relax
            11:15 PM - 4:45 AM Sleep & Night Rest
            
            ----------------------------------------
            Notes: Stay hydrated and focused.
            Page 1 of 1
            ========================================
        """.trimIndent()

        val tasks = parseSchedule(scheduleText)

        assertEquals(19, tasks.size, "Should parse exactly 19 tasks, ignoring headers and footers")

        // 1. Fajr Prayer & Morning Reflection (5:00 AM - 5:30 AM)
        assertEquals("Fajr Prayer & Morning Reflection", tasks[0].title)
        assertEquals(300, tasks[0].startMinutes)
        assertEquals(330, tasks[0].endMinutes)
        assertEquals(Category.PRAYER, tasks[0].category)
        assertFalse(tasks[0].crossesMidnight)

        // 2. Morning Gym & Workout (5:30 AM - 6:30 AM)
        assertEquals("Morning Gym & Workout", tasks[1].title)
        assertEquals(330, tasks[1].startMinutes)
        assertEquals(390, tasks[1].endMinutes)
        assertEquals(Category.REST, tasks[1].category)
        assertFalse(tasks[1].crossesMidnight)

        // 3. Breakfast & Coffee (6:30 AM - 7:15 AM)
        assertEquals("Breakfast & Coffee", tasks[2].title)
        assertEquals(390, tasks[2].startMinutes)
        assertEquals(435, tasks[2].endMinutes)
        assertEquals(Category.REST, tasks[2].category)
        assertFalse(tasks[2].crossesMidnight)

        // 4. DSA practice & Algorithms (7:15 AM - 8:45 AM, en dash separator)
        assertEquals("DSA practice & Algorithms", tasks[3].title)
        assertEquals(435, tasks[3].startMinutes)
        assertEquals(525, tasks[3].endMinutes)
        assertEquals(Category.LEARNING, tasks[3].category)
        assertFalse(tasks[3].crossesMidnight)

        // 5. Kotlin Study & Deep Dive (9:00 AM - 10:30 AM)
        assertEquals("Kotlin Study & Deep Dive", tasks[4].title)
        assertEquals(540, tasks[4].startMinutes)
        assertEquals(630, tasks[4].endMinutes)
        assertEquals(Category.LEARNING, tasks[4].category)
        assertFalse(tasks[4].crossesMidnight)

        // 6. Office Team Sync & Standup (10:30 AM - 11:30 AM)
        assertEquals("Office Team Sync & Standup", tasks[5].title)
        assertEquals(630, tasks[5].startMinutes)
        assertEquals(690, tasks[5].endMinutes)
        assertEquals(Category.JOBS, tasks[5].category)
        assertFalse(tasks[5].crossesMidnight)

        // 7. Client Meeting (11:30 AM - 12:30 PM)
        assertEquals("Client Meeting", tasks[6].title)
        assertEquals(690, tasks[6].startMinutes)
        assertEquals(750, tasks[6].endMinutes)
        assertEquals(Category.JOBS, tasks[6].category)
        assertFalse(tasks[6].crossesMidnight)

        // 8. Dhuhr Prayer & Lunch Break (12:30 PM - 1:30 PM)
        assertEquals("Dhuhr Prayer & Lunch Break", tasks[7].title)
        assertEquals(750, tasks[7].startMinutes)
        assertEquals(810, tasks[7].endMinutes)
        assertEquals(Category.PRAYER, tasks[7].category)
        assertFalse(tasks[7].crossesMidnight)

        // 9. Architecture & PR-Review (1:30 PM - 3:00 PM, title has hyphen)
        assertEquals("Architecture & PR-Review", tasks[8].title)
        assertEquals(810, tasks[8].startMinutes)
        assertEquals(900, tasks[8].endMinutes)
        assertEquals(Category.JOBS, tasks[8].category)
        assertFalse(tasks[8].crossesMidnight)

        // 10. Work on Bug-fixing Ticket (3:00 PM - 4:30 PM, em dash separator)
        assertEquals("Work on Bug-fixing Ticket", tasks[9].title)
        assertEquals(900, tasks[9].startMinutes)
        assertEquals(990, tasks[9].endMinutes)
        assertEquals(Category.JOBS, tasks[9].category)
        assertFalse(tasks[9].crossesMidnight)

        // 11. Asr Prayer Break (4:45 PM - 5:15 PM)
        assertEquals("Asr Prayer Break", tasks[10].title)
        assertEquals(1005, tasks[10].startMinutes)
        assertEquals(1035, tasks[10].endMinutes)
        assertEquals(Category.PRAYER, tasks[10].category)
        assertFalse(tasks[10].crossesMidnight)

        // 12. Evening Walk & Relax (5:15 PM - 6:00 PM)
        assertEquals("Evening Walk & Relax", tasks[11].title)
        assertEquals(1035, tasks[11].startMinutes)
        assertEquals(1080, tasks[11].endMinutes)
        assertEquals(Category.REST, tasks[11].category)
        assertFalse(tasks[11].crossesMidnight)

        // 13. Personal Project Research (6:00 PM - 7:15 PM)
        assertEquals("Personal Project Research", tasks[12].title)
        assertEquals(1080, tasks[12].startMinutes)
        assertEquals(1155, tasks[12].endMinutes)
        assertEquals(Category.LEARNING, tasks[12].category)
        assertFalse(tasks[12].crossesMidnight)

        // 14. Maghrib Prayer & Dinner (7:15 PM - 8:00 PM)
        assertEquals("Maghrib Prayer & Dinner", tasks[13].title)
        assertEquals(1155, tasks[13].startMinutes)
        assertEquals(1200, tasks[13].endMinutes)
        assertEquals(Category.PRAYER, tasks[13].category)
        assertFalse(tasks[13].crossesMidnight)

        // 15. Compose Multiplatform Course (8:00 PM - 9:00 PM)
        assertEquals("Compose Multiplatform Course", tasks[14].title)
        assertEquals(1200, tasks[14].startMinutes)
        assertEquals(1260, tasks[14].endMinutes)
        assertEquals(Category.LEARNING, tasks[14].category)
        assertFalse(tasks[14].crossesMidnight)

        // 16. Isha Prayer & Recitation (9:00 PM - 9:45 PM)
        assertEquals("Isha Prayer & Recitation", tasks[15].title)
        assertEquals(1260, tasks[15].startMinutes)
        assertEquals(1305, tasks[15].endMinutes)
        assertEquals(Category.PRAYER, tasks[15].category)
        assertFalse(tasks[15].crossesMidnight)

        // 17. Book Reading & Journaling (9:45 PM - 10:30 PM)
        assertEquals("Book Reading & Journaling", tasks[16].title)
        assertEquals(1305, tasks[16].startMinutes)
        assertEquals(1350, tasks[16].endMinutes)
        assertEquals(Category.LEARNING, tasks[16].category)
        assertFalse(tasks[16].crossesMidnight)

        // 18. Night Wind Down & Relax (10:30 PM - 11:15 PM)
        assertEquals("Night Wind Down & Relax", tasks[17].title)
        assertEquals(1350, tasks[17].startMinutes)
        assertEquals(1395, tasks[17].endMinutes)
        assertEquals(Category.REST, tasks[17].category)
        assertFalse(tasks[17].crossesMidnight)

        // 19. Sleep & Night Rest (11:15 PM - 4:45 AM, crosses midnight)
        assertEquals("Sleep & Night Rest", tasks[18].title)
        assertEquals(1395, tasks[18].startMinutes)
        assertEquals(285, tasks[18].endMinutes)
        assertEquals(Category.REST, tasks[18].category)
        assertTrue(tasks[18].crossesMidnight)
    }

    @Test
    fun testEnDashAndEmDashSeparators() {
        val input = """
            6:15 – 8:15 DSA practice
            9:00 — 11:00 Team Meeting
        """.trimIndent()

        val tasks = parseSchedule(input)
        assertEquals(2, tasks.size)

        assertEquals("DSA practice", tasks[0].title)
        assertEquals(375, tasks[0].startMinutes)
        assertEquals(495, tasks[0].endMinutes)
        assertEquals(Category.LEARNING, tasks[0].category)

        assertEquals("Team Meeting", tasks[1].title)
        assertEquals(540, tasks[1].startMinutes)
        assertEquals(660, tasks[1].endMinutes)
        assertEquals(Category.JOBS, tasks[1].category)
    }

    @Test
    fun testTitlesContainingHyphens() {
        val input = """
            2:00 - 3:30 PM Bug-fixing and Code-Review session
            4:00 - 5:00 PM CI-CD-Pipeline deployment
        """.trimIndent()

        val tasks = parseSchedule(input)
        assertEquals(2, tasks.size)

        assertEquals("Bug-fixing and Code-Review session", tasks[0].title)
        assertEquals("CI-CD-Pipeline deployment", tasks[1].title)
    }

    @Test
    fun testAmPmInferenceFromEndSuffix() {
        val input = """
            9:00 - 11:00 AM Morning Study
            11:00 - 1:00 PM Midday Sync
            1:00 - 2:30 PM Lunch & Rest
        """.trimIndent()

        val tasks = parseSchedule(input)
        assertEquals(3, tasks.size)

        // 9:00 AM - 11:00 AM
        assertEquals(540, tasks[0].startMinutes)
        assertEquals(660, tasks[0].endMinutes)

        // 11:00 AM - 1:00 PM
        assertEquals(660, tasks[1].startMinutes)
        assertEquals(780, tasks[1].endMinutes)

        // 1:00 PM - 2:30 PM
        assertEquals(780, tasks[2].startMinutes)
        assertEquals(870, tasks[2].endMinutes)
    }

    @Test
    fun testMidnightCrossingTasks() {
        val input = """
            11:00 PM - 5:00 AM Night Sleep & Rest
            11:30 PM – 6:15 AM Overnight Sleep
        """.trimIndent()

        val tasks = parseSchedule(input)
        assertEquals(2, tasks.size)

        assertEquals(1380, tasks[0].startMinutes)
        assertEquals(300, tasks[0].endMinutes)
        assertTrue(tasks[0].crossesMidnight)

        assertEquals(1410, tasks[1].startMinutes)
        assertEquals(375, tasks[1].endMinutes)
        assertTrue(tasks[1].crossesMidnight)
    }

    @Test
    fun testIgnoreNonScheduleLines() {
        val input = """
            Schedule for Monday
            ===================
            Morning routine
            9:00 - 10:00 AM Team Sync
            Note: Remember to bring laptop.
            Page 1 of 2
        """.trimIndent()

        val tasks = parseSchedule(input)
        assertEquals(1, tasks.size)
        assertEquals("Team Sync", tasks[0].title)
    }
}

