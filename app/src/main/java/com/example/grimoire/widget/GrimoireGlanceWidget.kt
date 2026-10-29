package com.example.grimoire.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.grimoire.MainActivity
import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.preferences.ThemePreferences
import com.example.grimoire.data.repository.HabitRepository
import com.example.grimoire.model.HabitUiModel
import com.example.grimoire.ui.theme.AppThemeMode

class GrimoireGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = GrimoireDatabase.getInstance(context)
        val repository = HabitRepository(database, context = context)
        val themePreferences = ThemePreferences(context)

        // Capture initial states so composition starts with current database & theme data
        val initialHabits = repository.getHabitsSync()
        val initialTheme = themePreferences.themeMode.value

        provideContent {
            // Observe Room DB flow and Theme Preference flow reactively in the widget
            val habits by repository.habitsFlow.collectAsState(initial = initialHabits)
            val currentTheme by themePreferences.themeMode.collectAsState(initial = initialTheme)
            val isSystemNight = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val palette = getWidgetPalette(currentTheme, isSystemNight)
            val phase = getCircadianPhaseWidget()

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(palette.backgroundColor)
                    .cornerRadius(24.dp)
                    .padding(12.dp)
            ) {
                // Header Row (Clicking launches MainActivity)
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grimoire",
                        style = TextStyle(
                            color = palette.accentGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Circadian Phase Badge
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(palette.cardColor)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${phase.icon} ${phase.title}",
                            style = TextStyle(
                                color = palette.primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Completed Today Counter Pill
                    val completedCount = habits.count { it.isCompletedToday }
                    val totalCount = habits.size
                    val isAllComplete = habits.isNotEmpty() && completedCount == totalCount

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(if (isAllComplete) palette.completedAccent else palette.cardColor)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦ $completedCount/$totalCount",
                            style = TextStyle(
                                color = if (isAllComplete) colorProvider(Color.Black) else palette.onSurfaceColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Habits List or Empty State
                if (habits.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✦ The Grimoire is Pristine ✦",
                                style = TextStyle(
                                    color = palette.accentGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = "Tap to inscribe your daily rituals",
                                style = TextStyle(
                                    color = palette.onSurfaceVariantColor,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        items(habits, itemId = { it.id }) { habit ->
                            WidgetHabitCard(
                                habit = habit,
                                palette = palette
                            )
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetHabitCard(
    habit: HabitUiModel,
    palette: WidgetPalette
) {
    val isComplete = habit.isCompletedToday
    val sigil = getHabitSigil(habit.iconName)
    val resonanceColor = parseHexColor(habit.colorHex)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (isComplete) palette.cardCompletedColor else palette.cardColor)
            .cornerRadius(14.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sigil with habit resonance tint
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(colorProvider(resonanceColor.copy(alpha = 0.2f)))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sigil,
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Title and Streak Details
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = habit.title,
                maxLines = 1,
                style = TextStyle(
                    color = palette.onSurfaceColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(top = 2.dp)
            ) {
                if (habit.currentStreak > 0) {
                    Text(
                        text = "🔥 ${habit.currentStreak}d  ",
                        style = TextStyle(
                            color = palette.accentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                if (habit.isFrozen) {
                    Text(
                        text = "🛡️  ",
                        style = TextStyle(fontSize = 11.sp)
                    )
                }
                Text(
                    text = if (isComplete) "✓ Sealed" else "${habit.completedTodayCount}/${habit.targetPerDay}",
                    style = TextStyle(
                        color = if (isComplete) palette.completedAccent else palette.onSurfaceVariantColor,
                        fontSize = 11.sp,
                        fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(6.dp))

        // Quick Interactive Increment & Decrement Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement Button
            Box(
                modifier = GlanceModifier
                    .cornerRadius(10.dp)
                    .background(palette.actionButtonBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(
                        actionRunCallback<DecrementHabitActionCallback>(
                            actionParametersOf(
                                HabitIdParamKey to habit.id,
                                RandomKey to java.util.UUID.randomUUID().toString()
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    style = TextStyle(
                        color = palette.actionButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Increment Button
            Box(
                modifier = GlanceModifier
                    .cornerRadius(10.dp)
                    .background(if (isComplete) palette.completedAccent else palette.accentGold)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(
                        actionRunCallback<IncrementHabitActionCallback>(
                            actionParametersOf(
                                HabitIdParamKey to habit.id,
                                RandomKey to java.util.UUID.randomUUID().toString()
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = colorProvider(Color.Black),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// Data class mapping Material 3 colors to Glance ColorProviders per AppThemeMode
data class WidgetPalette(
    val backgroundColor: ColorProvider,
    val cardColor: ColorProvider,
    val cardCompletedColor: ColorProvider,
    val primaryColor: ColorProvider,
    val onSurfaceColor: ColorProvider,
    val onSurfaceVariantColor: ColorProvider,
    val actionButtonBg: ColorProvider,
    val actionButtonText: ColorProvider,
    val accentGold: ColorProvider,
    val completedAccent: ColorProvider
)

private fun colorProvider(color: Color): ColorProvider = ColorProvider(color)

private fun getWidgetPalette(themeMode: AppThemeMode, isSystemNight: Boolean): WidgetPalette {
    val resolvedMode = if (themeMode == AppThemeMode.SYSTEM) {
        if (isSystemNight) AppThemeMode.DARK else AppThemeMode.LIGHT
    } else {
        themeMode
    }

    return when (resolvedMode) {
        AppThemeMode.LIGHT -> WidgetPalette(
            backgroundColor = colorProvider(Color(0xFFFCF8FF)),
            cardColor = colorProvider(Color(0xFFF0ECF4)),
            cardCompletedColor = colorProvider(Color(0xFFE8F5E9)),
            primaryColor = colorProvider(Color(0xFF6750A4)),
            onSurfaceColor = colorProvider(Color(0xFF1C1B1F)),
            onSurfaceVariantColor = colorProvider(Color(0xFF49454E)),
            actionButtonBg = colorProvider(Color(0xFFE2DDE8)),
            actionButtonText = colorProvider(Color(0xFF1C1B1F)),
            accentGold = colorProvider(Color(0xFFB8860B)),
            completedAccent = colorProvider(Color(0xFF2E7D32))
        )
        AppThemeMode.AMOLED -> WidgetPalette(
            backgroundColor = colorProvider(Color(0xFF000000)),
            cardColor = colorProvider(Color(0xFF0F0F0F)),
            cardCompletedColor = colorProvider(Color(0xFF0A2210)),
            primaryColor = colorProvider(Color(0xFFD4AF37)),
            onSurfaceColor = colorProvider(Color(0xFFEDEDED)),
            onSurfaceVariantColor = colorProvider(Color(0xFFB0B0B0)),
            actionButtonBg = colorProvider(Color(0xFF1C1C1C)),
            actionButtonText = colorProvider(Color(0xFFEDEDED)),
            accentGold = colorProvider(Color(0xFFD4AF37)),
            completedAccent = colorProvider(Color(0xFF4ADE80))
        )
        AppThemeMode.EXPRESSIVE -> WidgetPalette(
            backgroundColor = colorProvider(Color(0xFF0E0A17)),
            cardColor = colorProvider(Color(0xFF1F1730)),
            cardCompletedColor = colorProvider(Color(0xFF281530)),
            primaryColor = colorProvider(Color(0xFF9D65FF)),
            onSurfaceColor = colorProvider(Color(0xFFF9F5FF)),
            onSurfaceVariantColor = colorProvider(Color(0xFFD7CFE6)),
            actionButtonBg = colorProvider(Color(0xFF2D2146)),
            actionButtonText = colorProvider(Color(0xFFF9F5FF)),
            accentGold = colorProvider(Color(0xFFFF5277)),
            completedAccent = colorProvider(Color(0xFF00E5A3))
        )
        AppThemeMode.DARK, AppThemeMode.SYSTEM -> WidgetPalette(
            backgroundColor = colorProvider(Color(0xFF131316)),
            cardColor = colorProvider(Color(0xFF202024)),
            cardCompletedColor = colorProvider(Color(0xFF183320)),
            primaryColor = colorProvider(Color(0xFFD0BCFF)),
            onSurfaceColor = colorProvider(Color(0xFFE5E2E9)),
            onSurfaceVariantColor = colorProvider(Color(0xFFCAC4D0)),
            actionButtonBg = colorProvider(Color(0xFF2E2D34)),
            actionButtonText = colorProvider(Color(0xFFE5E2E9)),
            accentGold = colorProvider(Color(0xFFD4AF37)),
            completedAccent = colorProvider(Color(0xFF69F0AE))
        )
    }
}

private fun parseHexColor(hex: String, fallback: Color = Color(0xFFD4AF37)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

private fun getHabitSigil(iconName: String): String {
    return when (iconName.lowercase()) {
        "book", "study", "read" -> "📖"
        "water", "hydration", "drink" -> "💧"
        "fire", "flame", "vitality" -> "🔥"
        "fitness", "gym", "workout" -> "⚔️"
        "meditation", "mind", "zen" -> "🧘"
        "code", "dev", "hack", "craft" -> "⚡"
        "sleep", "rest" -> "🌙"
        "run", "walk", "jog" -> "🏃"
        "shield", "defense", "guard" -> "🛡️"
        else -> "✦"
    }
}

data class CircadianPhaseWidget(
    val title: String,
    val icon: String
)

private fun getCircadianPhaseWidget(): CircadianPhaseWidget {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 4..11 -> CircadianPhaseWidget("Dawn Vigil", "✦")
        in 12..17 -> CircadianPhaseWidget("Solar Zenith", "☀️")
        in 18..21 -> CircadianPhaseWidget("Dusk Ritual", "🌙")
        else -> CircadianPhaseWidget("Midnight Watch", "🛡")
    }
}

val HabitIdParamKey = ActionParameters.Key<Long>("habitId")
val RandomKey = ActionParameters.Key<String>("randomKey")

suspend fun updateGrimoireWidget(context: Context) {
    try {
        GrimoireGlanceWidget().updateAll(context)
    } catch (_: Exception) {}

    try {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GrimoireGlanceWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
            val widget = GrimoireGlanceWidget()
            for (id in appWidgetIds) {
                try {
                    widget.update(context, AppWidgetId(id))
                } catch (_: Exception) {}
            }
            val intent = Intent(context, GrimoireGlanceWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    } catch (_: Exception) {}
}

class IncrementHabitActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdParamKey] ?: return
        val database = GrimoireDatabase.getInstance(context)
        val repository = HabitRepository(database, context = context)
        repository.incrementHabit(habitId)
        try {
            GrimoireGlanceWidget().update(context, glanceId)
        } catch (_: Exception) {}
        updateGrimoireWidget(context)
    }
}

class DecrementHabitActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdParamKey] ?: return
        val database = GrimoireDatabase.getInstance(context)
        val repository = HabitRepository(database, context = context)
        repository.decrementHabit(habitId)
        try {
            GrimoireGlanceWidget().update(context, glanceId)
        } catch (_: Exception) {}
        updateGrimoireWidget(context)
    }
}
