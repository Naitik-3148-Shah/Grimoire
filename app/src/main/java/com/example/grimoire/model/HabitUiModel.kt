package com.example.grimoire.model

data class HabitUiModel(
    val id: Long = 0L,
    val title: String,
    val iconName: String,
    val colorHex: String,
    val targetPerDay: Int = 1,
    val completedTodayCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isFrozen: Boolean = false,
    val reminderTime: String? = null,
    val last30DaysHistory: List<Boolean> = List(30) { false }
) {
    val isCompletedToday: Boolean
        get() = completedTodayCount >= targetPerDay

    val completionProgress: Float
        get() = if (targetPerDay > 0) (completedTodayCount.toFloat() / targetPerDay).coerceIn(0f, 1f) else 0f
}