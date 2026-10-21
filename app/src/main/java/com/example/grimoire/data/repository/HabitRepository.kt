package com.example.grimoire.data.repository

import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.local.HabitDao
import com.example.grimoire.data.local.HabitEntity
import com.example.grimoire.data.local.HabitLogEntity
import com.example.grimoire.data.local.HabitWithLogs
import com.example.grimoire.model.HabitUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val database: GrimoireDatabase,
    private val habitDao: HabitDao = database.habitDao(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val transactionMutex = Mutex()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    val habitsFlow: Flow<List<HabitUiModel>> = habitDao.observeAllWithLogs()
        .map { list -> list.map { it.toUiModel() } }
        .flowOn(ioDispatcher)

    suspend fun addHabit(
        title: String,
        iconName: String,
        colorHex: String,
        targetPerDay: Int,
        reminderTime: String?
    ) = withContext(ioDispatcher) {
        val habit = HabitEntity(
            title = title,
            iconName = iconName,
            colorHex = colorHex,
            targetPerDay = targetPerDay.coerceAtLeast(1),
            reminderTime = reminderTime
        )
        habitDao.insertHabit(habit)
    }

    suspend fun incrementHabit(habitId: Long) = withContext(ioDispatcher) {
        transactionMutex.withLock {
            val today = LocalDate.now().format(dateFormatter)
            val habit = habitDao.getHabitById(habitId) ?: return@withContext
            val currentLog = habitDao.getLogForDate(habitId, today)
            val currentCount = currentLog?.completedCount ?: 0
            val newCount = currentCount + 1

            // 1. Commit the append-only log record
            val logToSave = currentLog?.copy(
                completedCount = newCount,
                updatedTimestamp = System.currentTimeMillis()
            ) ?: HabitLogEntity(
                habitId = habitId,
                dateString = today,
                completedCount = newCount
            )
            habitDao.insertLog(logToSave)

            // 2. Evaluate streak trigger: only increment streak when threshold is crossed for the first time today
            if (currentCount < habit.targetPerDay && newCount >= habit.targetPerDay) {
                val updatedStreak = habit.currentStreak + 1
                val updatedLongest = maxOf(habit.longestStreak, updatedStreak)
                habitDao.updateHabit(
                    habit.copy(
                        currentStreak = updatedStreak,
                        longestStreak = updatedLongest
                    )
                )
            }
        }
    }

    suspend fun decrementHabit(habitId: Long) = withContext(ioDispatcher) {
        transactionMutex.withLock {
            val today = LocalDate.now().format(dateFormatter)
            val habit = habitDao.getHabitById(habitId) ?: return@withContext
            val currentLog = habitDao.getLogForDate(habitId, today) ?: return@withContext
            if (currentLog.completedCount <= 0) return@withContext

            val wasTargetMet = currentLog.completedCount >= habit.targetPerDay
            val newCount = currentLog.completedCount - 1
            habitDao.insertLog(
                currentLog.copy(
                    completedCount = newCount,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )

            // Decrement streak if drops below daily target
            if (wasTargetMet && newCount < habit.targetPerDay) {
                val updatedStreak = (habit.currentStreak - 1).coerceAtLeast(0)
                habitDao.updateHabit(habit.copy(currentStreak = updatedStreak))
            }
        }
    }

    suspend fun toggleFreezeShield(habitId: Long) = withContext(ioDispatcher) {
        val habit = habitDao.getHabitById(habitId) ?: return@withContext
        habitDao.updateHabit(habit.copy(isFrozen = !habit.isFrozen))
    }

    suspend fun deleteHabit(habitId: Long) = withContext(ioDispatcher) {
        val habit = habitDao.getHabitById(habitId) ?: return@withContext
        habitDao.deleteHabit(habit)
    }

    // Helper extension mapping Entities -> Shared UI Contract
    private fun HabitWithLogs.toUiModel(): HabitUiModel {
        val today = LocalDate.now()
        val todayString = today.format(dateFormatter)
        val logsMap = logs.associateBy { it.dateString }

        val todayLog = logsMap[todayString]
        val completedToday = todayLog?.completedCount ?: 0

        // Precompute 30-day heatmap booleans (index 0 = 29 days ago, index 29 = today)
        val history30 = (29 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong()).format(dateFormatter)
            val count = logsMap[date]?.completedCount ?: 0
            count >= habit.targetPerDay
        }

        return HabitUiModel(
            id = habit.id,
            title = habit.title,
            iconName = habit.iconName,
            colorHex = habit.colorHex,
            targetPerDay = habit.targetPerDay,
            completedTodayCount = completedToday,
            currentStreak = habit.currentStreak,
            longestStreak = habit.longestStreak,
            isFrozen = habit.isFrozen,
            reminderTime = habit.reminderTime,
            last30DaysHistory = history30
        )
    }
}