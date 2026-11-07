package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.core.util.dateTicker
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.progress.DayStats
import com.saif.scheduleapp.domain.progress.Stats
import com.saif.scheduleapp.domain.progress.currentStreak
import com.saif.scheduleapp.domain.progress.longestStreak
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetStats(
    private val repository: ScheduleRepository,
    private val dateFlow: Flow<LocalDate> = dateTicker()
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Stats> {
        return dateFlow.flatMapLatest { today ->
            invoke(today)
        }.flowOn(Dispatchers.Default)
    }

    operator fun invoke(today: LocalDate): Flow<Stats> {
        val from = today.minus(365, DateTimeUnit.DAY)
        val to = today
        return combine(
            repository.observeAllTasks(),
            repository.observeCompletions(from, to)
        ) { allTasks, completionsMap ->
            computeStats(today, allTasks, completionsMap)
        }.flowOn(Dispatchers.Default)
    }

    private fun computeStats(
        today: LocalDate,
        allTasks: List<ScheduleTask>,
        completionsMap: Map<LocalDate, Set<Long>>
    ): Stats {
        fun dayStatsFor(date: LocalDate): DayStats {
            val activeTasks = allTasks.filter { it.isActiveOn(date) }
            val completedIds = completionsMap[date] ?: emptySet()
            val doneCount = activeTasks.count { it.id in completedIds }
            return DayStats(
                date = date,
                total = activeTasks.size,
                done = doneCount
            )
        }

        val todayStats = dayStatsFor(today)

        val last7 = (6 downTo 0).map { offset ->
            dayStatsFor(today.minus(offset, DateTimeUnit.DAY))
        }

        val last30 = (29 downTo 0).map { offset ->
            dayStatsFor(today.minus(offset, DateTimeUnit.DAY))
        }

        val all365 = (365 downTo 0).map { offset ->
            dayStatsFor(today.minus(offset, DateTimeUnit.DAY))
        }

        val completedLast30 = last30.sumOf { it.done }
        val averageLast30 = completedLast30.toFloat() / 30f

        return Stats(
            today = todayStats,
            last7 = last7,
            currentStreak = currentStreak(all365, today),
            longestStreak = longestStreak(all365),
            completedLast30 = completedLast30,
            averageLast30 = averageLast30
        )
    }
}

