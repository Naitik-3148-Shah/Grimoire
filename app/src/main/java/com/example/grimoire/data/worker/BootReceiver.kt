package com.example.grimoire.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.grimoire.data.local.GrimoireDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = GrimoireDatabase.getInstance(context)
                    val habits = db.habitDao().getAllHabitsSync()
                    for (habit in habits) {
                        habit.reminderTime?.let { time ->
                            AlarmScheduler.scheduleHabitAlarm(context, habit.id, habit.title, time)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}