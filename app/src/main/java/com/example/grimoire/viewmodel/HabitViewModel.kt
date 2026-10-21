package com.example.grimoire.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.repository.HabitRepository
import com.example.grimoire.model.HabitEvent
import com.example.grimoire.model.HabitUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GrimoireDatabase.getInstance(application)
    private val repository = HabitRepository(database)

    // Exposes the exact StateFlow consumed by Naitik Maurya's Compose UI
    val habits: StateFlow<List<HabitUiModel>> = repository.habitsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onEvent(event: HabitEvent) {
        viewModelScope.launch {
            when (event) {
                is HabitEvent.AddHabit -> {
                    repository.addHabit(
                        title = event.title,
                        iconName = event.iconName,
                        colorHex = event.colorHex,
                        targetPerDay = event.targetPerDay,
                        reminderTime = event.reminderTime
                    )
                }
                is HabitEvent.IncrementHabit -> {
                    repository.incrementHabit(event.habitId)
                }
                is HabitEvent.DecrementHabit -> {
                    repository.decrementHabit(event.habitId)
                }
                is HabitEvent.ToggleFreezeShield -> {
                    repository.toggleFreezeShield(event.habitId)
                }
                is HabitEvent.DeleteHabit -> {
                    repository.deleteHabit(event.habitId)
                }
                is HabitEvent.ExportVaultToUri -> {
                    // Handled in Phase 5 via SovereignVaultManager
                }
                is HabitEvent.ImportVaultFromUri -> {
                    // Handled in Phase 5 via SovereignVaultManager
                }
            }
        }
    }
}