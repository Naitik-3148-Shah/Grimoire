package com.example.grimoire

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grimoire.ui.theme.GrimoireTheme
import com.example.grimoire.viewmodel.HabitViewModel
import com.sorcerers.grimoire.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable 120Hz high refresh rate display mode on supported hardware
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val maxMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
            if (maxMode != null) {
                window.attributes.preferredDisplayModeId = maxMode.modeId
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.attributes.preferredRefreshRate = 120f
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val viewModel: HabitViewModel = viewModel()
            val currentTheme by viewModel.themeMode.collectAsState()
            val habits by viewModel.habits.collectAsState()

            GrimoireTheme(themeMode = currentTheme) {
                val surfaceColor = MaterialTheme.colorScheme.surface
                SideEffect {
                    window.setBackgroundDrawable(surfaceColor.toArgb().toDrawable())
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = surfaceColor
                ) {
                    MainScreen(
                        habits = habits,
                        currentTheme = currentTheme,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}
