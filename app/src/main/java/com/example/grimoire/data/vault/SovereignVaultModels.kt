package com.example.grimoire.data.vault

import kotlinx.serialization.Serializable

@Serializable
data class HabitBackupDto(
    val id: Long,
    val title: String,
    val iconName: String,
    val colorHex: String,
    val targetPerDay: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val isFrozen: Boolean,
    val reminderTime: String?,
    val createdAt: Long
)

@Serializable
data class HabitLogBackupDto(
    val logId: Long,
    val habitId: Long,
    val dateString: String,
    val completedCount: Int,
    val updatedTimestamp: Long
)

@Serializable
data class SovereignVaultPayload(
    val schemaVersion: Int = 1,
    val exportedAtEpochMs: Long = System.currentTimeMillis(),
    val habits: List<HabitBackupDto>,
    val logs: List<HabitLogBackupDto>
)