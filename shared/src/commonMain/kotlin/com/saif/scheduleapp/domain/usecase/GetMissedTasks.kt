package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.core.util.dateTicker
import com.saif.scheduleapp.core.util.minuteTicker
import com.saif.scheduleapp.domain.model.MissedTask
import com.saif.scheduleapp.domain.model.ScheduleTask
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

class GetMissedTasks(
    private val repository: ScheduleRepository,
    private val dateFlow: Flow<LocalDate> = dateTicker(),
    private val minuteFlow: Flow<Int> = minuteTicker(),
    private val lookbackDays: Int = 7
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<MissedTask>> {
        return combine(dateFlow, minuteFlow) { today, nowMinutes ->
            today to nowMinutes
        }.flatMapLatest { (today, nowMinutes) ->
            invoke(today, nowMinutes)
        }.flowOn(Dispatchers.Default)
    }

    operator fun invoke(today: LocalDate, nowMinutes: Int): Flow<List<MissedTask>> {
        val from = if (lookbackDays > 0) today.minus(lookbackDays - 1, DateTimeUnit.DAY) else today
        val to = today
        return combine(
            repository.observeAllTasks(),
            repository.observeCompletions(from, to)
        ) { allTasks, completionsMap ->
            computeMissedTasks(allTasks, completionsMap, today, nowMinutes, lookbackDays)
        }.flowOn(Dispatchers.Default)
    }

    companion object {
        fun computeMissedTasks(
            allTasks: List<ScheduleTask>,
            completionsMap: Map<LocalDate, Set<Long>>,
            today: LocalDate,
            nowMinutes: Int,
            lookbackDays: Int = 7
        ): List<MissedTask> {
            val fromDate = if (lookbackDays > 0) today.minus(lookbackDays - 1, DateTimeUnit.DAY) else today
            val missedList = mutableListOf<MissedTask>()

            var d = today
            while (d >= fromDate) {
                val completedIds = completionsMap[d] ?: emptySet()
                val isToday = (d == today)

                val activeTasks = allTasks.filter { it.isActiveOn(d) }
                for (task in activeTasks) {
                    val isDone = task.id in completedIds
                    if (!isDone) {
                        val isExpired = if (isToday) {
                            nowMinutes >= task.endMinutes
                        } else {
                            true
                        }
                        if (isExpired) {
                            missedList.add(MissedTask(task = task, date = d))
                        }
                    }
                }
                d = d.minus(1, DateTimeUnit.DAY)
            }

            // Return sorted: today first, then descending by date, and ascending by start time within each date
            return missedList.sortedWith(
                compareByDescending<MissedTask> { it.date }
                    .thenBy { it.task.startMinutes }
            )
        }
    }
}
