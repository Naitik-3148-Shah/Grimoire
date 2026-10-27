package com.example.grimoire.model

import com.example.grimoire.ui.theme.AppThemeMode

sealed interface HabitEvent {
    data class AddHabit(
        val title: String,
        val iconName: String,
        val colorHex: String,
        val targetPerDay: Int = 1,
        val reminderTime: String? = null
    ) : HabitEvent

    data class EditHabit(
        val habitId: Long,
        val title: String,
        val iconName: String,
        val colorHex: String,
        val targetPerDay: Int,
        val reminderTime: String?
    ) : HabitEvent

    data class IncrementHabit(val habitId: Long) : HabitEvent
    data class DecrementHabit(val habitId: Long) : HabitEvent
    data class ToggleFreezeShield(val habitId: Long) : HabitEvent
    data class DeleteHabit(val habitId: Long) : HabitEvent
    data class ExportVaultToUri(val uriString: String) : HabitEvent
    data class ImportVaultFromUri(val uriString: String) : HabitEvent
    data class SetThemeMode(val themeMode: AppThemeMode) : HabitEvent
}