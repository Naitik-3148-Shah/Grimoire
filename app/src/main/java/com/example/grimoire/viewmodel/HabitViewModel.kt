package com.example.grimoire.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.repository.HabitRepository
import com.example.grimoire.data.vault.SovereignVaultManager
import com.example.grimoire.model.HabitEvent
import com.example.grimoire.model.HabitUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.net.toUri

sealed interface VaultNotification {
    data class Success(val message: String) : VaultNotification
    data class Error(val error: String) : VaultNotification
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GrimoireDatabase.getInstance(application)
    private val repository = HabitRepository(database, context = application)
    private val vaultManager = SovereignVaultManager(application, database)

    // Reactive stream observed by Naitik Maurya's Compose UI & Widgets
    val habits: StateFlow<List<HabitUiModel>> = repository.habitsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // One-time notification stream for Snackbars/Toast on export or restore
    private val _vaultNotification = MutableSharedFlow<VaultNotification>()
    val vaultNotification: SharedFlow<VaultNotification> = _vaultNotification.asSharedFlow()

    fun onEvent(event: HabitEvent) {
        viewModelScope.launch {
            when (event) {
                is HabitEvent.AddHabit -> {
                    val habitId = repository.addHabit(
                        title = event.title,
                        iconName = event.iconName,
                        colorHex = event.colorHex,
                        targetPerDay = event.targetPerDay,
                        reminderTime = event.reminderTime
                    )
                    event.reminderTime?.let { time ->
                        com.example.grimoire.data.worker.AlarmScheduler.scheduleHabitAlarm(
                            getApplication(), habitId, event.title, time
                        )
                    }
                }
                is HabitEvent.EditHabit -> {
                    repository.updateHabit(
                        habitId = event.habitId,
                        title = event.title,
                        iconName = event.iconName,
                        colorHex = event.colorHex,
                        targetPerDay = event.targetPerDay,
                        reminderTime = event.reminderTime
                    )
                    if (event.reminderTime != null) {
                        com.example.grimoire.data.worker.AlarmScheduler.scheduleHabitAlarm(
                            getApplication(), event.habitId, event.title, event.reminderTime
                        )
                    } else {
                        com.example.grimoire.data.worker.AlarmScheduler.cancelHabitAlarm(
                            getApplication(), event.habitId
                        )
                    }
                }
                is HabitEvent.IncrementHabit -> repository.incrementHabit(event.habitId)
                is HabitEvent.DecrementHabit -> repository.decrementHabit(event.habitId)
                is HabitEvent.ToggleFreezeShield -> repository.toggleFreezeShield(event.habitId)
                is HabitEvent.DeleteHabit -> {
                    repository.deleteHabit(event.habitId)
                    com.example.grimoire.data.worker.AlarmScheduler.cancelHabitAlarm(
                        getApplication(), event.habitId
                    )
                }

                // Task A.3: Sovereign Vault Export Event Execution
                is HabitEvent.ExportVaultToUri -> {
                    val uri = event.uriString.toUri()
                    val result = vaultManager.exportVault(uri)
                    result.fold(
                        onSuccess = {
                            _vaultNotification.emit(
                                VaultNotification.Success("Sovereign Vault archived to local storage.")
                            )
                        },
                        onFailure = { error ->
                            _vaultNotification.emit(
                                VaultNotification.Error("Export failed: ${error.localizedMessage ?: "Unknown I/O error"}")
                            )
                        }
                    )
                }

                // Task A.3: Sovereign Vault Import Event Execution
                is HabitEvent.ImportVaultFromUri -> {
                    val uri = event.uriString.toUri()
                    val result = vaultManager.importVault(uri)
                    result.fold(
                        onSuccess = {
                            _vaultNotification.emit(
                                VaultNotification.Success("Sovereign Vault restored successfully.")
                            )
                        },
                        onFailure = { error ->
                            _vaultNotification.emit(
                                VaultNotification.Error("Restore rejected: Corrupted schema or invalid JSON.")
                            )
                        }
                    )
                }
            }
        }
    }
}