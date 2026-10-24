package com.example.grimoire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grimoire.ui.theme.GrimoireTheme
import com.example.grimoire.viewmodel.HabitViewModel
import com.sorcerers.grimoire.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrimoireTheme {
                val viewModel: HabitViewModel = viewModel()
                val habits by viewModel.habits.collectAsState()

                MainScreen(
                    habits = habits,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}
