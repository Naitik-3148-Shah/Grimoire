package com.example.grimoire.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.grimoire.data.local.GrimoireDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.glance.appwidget.updateAll

class MidnightRolloverWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = GrimoireDatabase.getInstance(applicationContext)
        val habitDao = database.habitDao()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        // Yesterday's date
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        val habits = habitDao.getAllHabitsSync()

        for (habit in habits) {
            val yesterdayLog = habitDao.getLogForDate(habit.id, yesterday)
            val wasTargetMet = (yesterdayLog?.completedCount ?: 0) >= habit.targetPerDay

            if (!wasTargetMet) {
                if (habit.isFrozen) {
                    // Shield protects the streak; shield is consumed for subsequent days
                    habitDao.updateHabit(habit.copy(isFrozen = false))
                } else {
                    // Unprotected break resets current streak to 0
                    if (habit.currentStreak > 0) {
                        habitDao.updateHabit(habit.copy(currentStreak = 0))
                    }
                }
            }
        }

        com.example.grimoire.widget.updateGrimoireWidget(applicationContext)
        
        Result.success()
    }
}