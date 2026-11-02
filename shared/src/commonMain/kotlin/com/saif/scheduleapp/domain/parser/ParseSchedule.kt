package com.saif.scheduleapp.domain.parser

import com.saif.scheduleapp.domain.model.Category

data class ParsedTask(
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val category: Category,
    val crossesMidnight: Boolean = false
)

private val TIME_LINE_REGEX = Regex(
    """^\s*(\d{1,2}(?::\d{2})?)\s*(am|pm|a\.m\.|p\.m\.)?\s*[-–—]\s*(\d{1,2}(?::\d{2})?)\s*(am|pm|a\.m\.|p\.m\.)?(?:\s+(.*)|$)""",
    RegexOption.IGNORE_CASE
)

fun parseSchedule(text: String): List<ParsedTask> {
    val results = mutableListOf<ParsedTask>()
    var cursor = 0 // running timeline cursor in minutes of day

    for (rawLine in text.lines()) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue

        val match = TIME_LINE_REGEX.matchEntire(line) ?: continue

        val startRaw = match.groupValues[1]
        val startAmPmRaw = match.groupValues[2].takeIf { it.isNotEmpty() }
        val endRaw = match.groupValues[3]
        val endAmPmRaw = match.groupValues[4].takeIf { it.isNotEmpty() }
        val rawTitle = match.groupValues[5].trim()

        if (rawTitle.isEmpty()) continue

        val (startH, startM) = parseHourMinute(startRaw)
        val (endH, endM) = parseHourMinute(endRaw)

        val startAmPmNormalized = normalizeAmPm(startAmPmRaw)
        val endAmPmNormalized = normalizeAmPm(endAmPmRaw)

        val (startMinutes, endMinutes, crossesMidnight) = resolveTimeRange(
            startH = startH,
            startM = startM,
            startAmPm = startAmPmNormalized,
            endH = endH,
            endM = endM,
            endAmPm = endAmPmNormalized,
            cursor = cursor
        )

        val category = matchCategory(rawTitle)

        results.add(
            ParsedTask(
                title = rawTitle,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                category = category,
                crossesMidnight = crossesMidnight
            )
        )

        // Advance timeline cursor
        cursor = if (crossesMidnight) {
            endMinutes // now in the next day's morning
        } else {
            endMinutes
        }
    }

    return results
}

private fun parseHourMinute(timeStr: String): Pair<Int, Int> {
    val parts = timeStr.split(":")
    val hour = parts[0].toIntOrNull() ?: 0
    val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
    return hour to minute
}

private fun normalizeAmPm(amPm: String?): String? {
    if (amPm == null) return null
    val clean = amPm.replace(".", "").trim().lowercase()
    return when (clean) {
        "am" -> "AM"
        "pm" -> "PM"
        else -> null
    }
}

private fun toMinutesOfDay(hour12: Int, minute: Int, isPm: Boolean): Int {
    val hour24 = when {
        hour12 == 12 && !isPm -> 0
        hour12 == 12 && isPm -> 12
        isPm -> hour12 + 12
        else -> hour12
    }
    return hour24 * 60 + minute
}

private fun resolveTimeRange(
    startH: Int,
    startM: Int,
    startAmPm: String?,
    endH: Int,
    endM: Int,
    endAmPm: String?,
    cursor: Int
): Triple<Int, Int, Boolean> {
    val startIsAmExplicit = startAmPm == "AM"
    val startIsPmExplicit = startAmPm == "PM"
    val endIsAmExplicit = endAmPm == "AM"
    val endIsPmExplicit = endAmPm == "PM"

    var startIsPm: Boolean
    var endIsPm: Boolean

    if (startAmPm != null && endAmPm != null) {
        startIsPm = startIsPmExplicit
        endIsPm = endIsPmExplicit
    } else if (startAmPm == null && endAmPm != null) {
        endIsPm = endIsPmExplicit
        if (!endIsPm) {
            // End is AM -> start must also be AM
            startIsPm = false
        } else {
            // End is PM
            val startAsAm = toMinutesOfDay(startH, startM, isPm = false)
            val startAsPm = toMinutesOfDay(startH, startM, isPm = true)
            val endAsPm = toMinutesOfDay(endH, endM, isPm = true)

            startIsPm = if (startAsPm > endAsPm) {
                // e.g. 11:00 - 1:00 PM -> 11:00 PM > 1:00 PM, so start must be AM
                false
            } else if (cursor >= 720) {
                // Already in afternoon
                true
            } else if (startAsAm < cursor) {
                // start AM is behind cursor, so start is PM
                true
            } else {
                // start is before noon, but both fit in PM e.g. 1:00 - 3:00 PM
                // If startAsAm is early morning (e.g. 1:00 AM) and cursor is e.g. noon
                // If start is e.g. 1:00 and end is 2:00 PM, usually both are PM
                true
            }
        }
    } else if (startAmPm != null && endAmPm == null) {
        startIsPm = startIsPmExplicit
        if (startIsPm) {
            // Start is PM: e.g. 1:00 PM - 3:00 -> end is PM (3:00 PM)
            // unless 11:30 PM - 5:00 -> end is AM (next day)
            val endAsPm = toMinutesOfDay(endH, endM, isPm = true)
            val startAsPm = toMinutesOfDay(startH, startM, isPm = true)
            endIsPm = endAsPm >= startAsPm
        } else {
            // Start is AM: e.g. 9:00 AM - 11:00 -> end is AM (11:00 AM)
            // e.g. 11:00 AM - 1:00 -> end is PM (1:00 PM)
            val startAsAm = toMinutesOfDay(startH, startM, isPm = false)
            val endAsAm = toMinutesOfDay(endH, endM, isPm = false)
            endIsPm = endAsAm < startAsAm
        }
    } else {
        // Neither has AM/PM suffix -> Infer entirely from cursor and progression
        val startAsAm = toMinutesOfDay(startH, startM, isPm = false)
        val startAsPm = toMinutesOfDay(startH, startM, isPm = true)

        startIsPm = when {
            // If AM time is already behind cursor, must be PM
            startAsAm < cursor && startAsPm >= cursor -> true
            // If already afternoon (cursor >= 720)
            cursor >= 720 -> true
            // Standard morning hours (e.g. 4..11) before noon
            startH in 4..11 && cursor < 720 -> false
            // 12:00 when cursor is morning: 12:00 PM (noon)
            startH == 12 && cursor in 300..719 -> true
            // Otherwise default to AM if it's forward of cursor
            startAsAm >= cursor -> false
            else -> true
        }

        val resolvedStart = toMinutesOfDay(startH, startM, isPm = startIsPm)
        val endAsAm = toMinutesOfDay(endH, endM, isPm = false)
        val endAsPm = toMinutesOfDay(endH, endM, isPm = true)

        endIsPm = if (startIsPm) {
            endAsPm >= resolvedStart
        } else {
            // Start was AM
            if (endAsAm >= resolvedStart) {
                false
            } else {
                endAsPm >= resolvedStart
            }
        }
    }

    val startMinutes = toMinutesOfDay(startH, startM, isPm = startIsPm)
    val endMinutes = toMinutesOfDay(endH, endM, isPm = endIsPm)

    val crossesMidnight = startMinutes > endMinutes || (startIsPm && !endIsPm)

    return Triple(startMinutes, endMinutes, crossesMidnight)
}

private fun matchCategory(title: String): Category {
    val lower = title.lowercase()

    // 1. PRAYER (highest priority)
    val prayerKeywords = listOf(
        "prayer", "fajr", "dhuhr", "zuhr", "asr", "maghrib", "isha",
        "salah", "salaah", "namaz", "quran", "recitation", "dhikr",
        "tahajjud", "taraweeh", "jummah", "masjid", "mosque", "dua",
        "supplication", "reflection"
    )
    if (prayerKeywords.any { lower.containsKeyword(it) }) {
        return Category.PRAYER
    }

    // 2. REST (sleep, meals, breaks)
    val restKeywords = listOf(
        "sleep", "rest", "nap", "break", "lunch", "dinner",
        "breakfast", "meal", "relax", "relaxation", "wind down",
        "walk", "gym", "workout", "exercise"
    )
    if (restKeywords.any { lower.containsKeyword(it) }) {
        return Category.REST
    }

    // 3. JOBS (work, client, office, meetings, architecture)
    val jobsKeywords = listOf(
        "work", "client", "office", "meeting", "standup", "sync",
        "shift", "architecture", "code review", "code-review",
        "pr review", "pr-review", "ticket", "jira", "deploy",
        "business", "job", "jobs"
    )
    if (jobsKeywords.any { lower.containsKeyword(it) }) {
        return Category.JOBS
    }

    // 4. LEARNING (study, dsa, learn, practice, research, course)
    val learningKeywords = listOf(
        "study", "learning", "learn", "dsa", "algorithm", "algorithms",
        "practice", "tutorial", "course", "class", "lecture",
        "homework", "research", "revision", "exam", "reading",
        "read", "book", "deep dive", "kotlin", "compose"
    )
    if (learningKeywords.any { lower.containsKeyword(it) }) {
        return Category.LEARNING
    }

    // Default fallback
    return Category.LEARNING
}

private fun String.containsKeyword(keyword: String): Boolean {
    val escaped = Regex.escape(keyword)
    val regex = Regex("""(?i)(?:^|[\s\-_/&,.:;()\[\]])$escaped(?:${'$'}|[\s\-_/&,.:;()\[\]])""")
    return regex.containsMatchIn(this)
}

