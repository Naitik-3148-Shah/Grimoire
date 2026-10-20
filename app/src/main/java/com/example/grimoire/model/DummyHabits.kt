package com.example.grimoire.model

object DummyHabits {
    val sampleList = listOf(
        HabitUiModel(
            id = 1L,
            title = "Study Arcane Algorithms",
            iconName = "book",
            colorHex = "#D4AF37",
            targetPerDay = 1,
            completedTodayCount = 1,
            currentStreak = 12,
            longestStreak = 15,
            isFrozen = false,
            reminderTime = "09:00",
            last30DaysHistory = List(30) { it % 2 == 0 }
        ),
        HabitUiModel(
            id = 2L,
            title = "Elixir of Hydration",
            iconName = "water",
            colorHex = "#29B6F6",
            targetPerDay = 8,
            completedTodayCount = 5,
            currentStreak = 4,
            longestStreak = 10,
            isFrozen = true,
            reminderTime = "14:00",
            last30DaysHistory = List(30) { true }
        )
    )
}