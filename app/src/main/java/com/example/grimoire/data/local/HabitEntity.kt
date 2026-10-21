package com.example.grimoire.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val iconName: String,
    val colorHex: String,
    val targetPerDay: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isFrozen: Boolean = false,
    val reminderTime: String? = null, // "HH:mm"
    val createdAt: Long = System.currentTimeMillis()
)