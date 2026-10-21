package com.example.grimoire

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.grimoire.data.worker.MidnightRolloverWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class GrimoireApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleMidnightWork()
    }

    private fun scheduleMidnightWork() {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val initialDelayMinutes = Duration.between(now, nextMidnight).toMinutes().coerceAtLeast(1)

        val midnightWorkRequest = PeriodicWorkRequestBuilder<MidnightRolloverWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "grimoire_midnight_rollover",
            ExistingPeriodicWorkPolicy.KEEP,
            midnightWorkRequest
        )
    }
}