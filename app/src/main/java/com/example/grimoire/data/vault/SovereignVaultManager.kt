package com.example.grimoire.data.vault

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.local.HabitEntity
import com.example.grimoire.data.local.HabitLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class SovereignVaultManager(
    private val context: Context,
    private val database: GrimoireDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializes all tables into an indented JSON file saved directly to user storage via SAF URI.
     */
    suspend fun exportVault(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dao = database.habitDao()
            val habits = dao.getAllHabitsSync()
            val logs = dao.getAllLogsSync()

            val payload = SovereignVaultPayload(
                habits = habits.map {
                    HabitBackupDto(
                        id = it.id,
                        title = it.title,
                        iconName = it.iconName,
                        colorHex = it.colorHex,
                        targetPerDay = it.targetPerDay,
                        currentStreak = it.currentStreak,
                        longestStreak = it.longestStreak,
                        isFrozen = it.isFrozen,
                        reminderTime = it.reminderTime,
                        createdAt = it.createdAt
                    )
                },
                logs = logs.map {
                    HabitLogBackupDto(
                        logId = it.logId,
                        habitId = it.habitId,
                        dateString = it.dateString,
                        completedCount = it.completedCount,
                        updatedTimestamp = it.updatedTimestamp
                    )
                }
            )

            val jsonString = json.encodeToString(payload)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Unable to open output stream for URI: $destinationUri")
        }
    }

    /**
     * Atomically clears and restores the database from a JSON file.
     * If parsing fails or data is corrupted, Room's withTransaction automatically rolls back.
     */
    suspend fun importVault(sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Unable to open input stream for URI: $sourceUri")

            val payload = json.decodeFromString<SovereignVaultPayload>(rawJson)
            val dao = database.habitDao()

            database.withTransaction {
                // Clear existing tables
                dao.clearAllLogs()
                dao.clearAllHabits()

                // Insert restored habits
                val habitEntities = payload.habits.map {
                    HabitEntity(
                        id = it.id,
                        title = it.title,
                        iconName = it.iconName,
                        colorHex = it.colorHex,
                        targetPerDay = it.targetPerDay,
                        currentStreak = it.currentStreak,
                        longestStreak = it.longestStreak,
                        isFrozen = it.isFrozen,
                        reminderTime = it.reminderTime,
                        createdAt = it.createdAt
                    )
                }
                dao.insertHabitsBatch(habitEntities)

                // Insert restored logs
                val logEntities = payload.logs.map {
                    HabitLogEntity(
                        logId = it.logId,
                        habitId = it.habitId,
                        dateString = it.dateString,
                        completedCount = it.completedCount,
                        updatedTimestamp = it.updatedTimestamp
                    )
                }
                dao.insertLogsBatch(logEntities)
            }
        }
    }
}