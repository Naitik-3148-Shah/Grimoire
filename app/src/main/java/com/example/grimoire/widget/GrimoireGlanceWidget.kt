package com.example.grimoire.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.grimoire.data.local.GrimoireDatabase
import com.example.grimoire.data.repository.HabitRepository

class GrimoireGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = GrimoireDatabase.getInstance(context)
        val repository = HabitRepository(database, context = context)
        val habits = repository.getHabitsSync()
        
        val hour = java.time.LocalTime.now().hour
        val phase = when (hour) {
            in 4..11 -> CircadianPhaseWidget("Dawn Vigil", "✦", Color(0xFFFF8F00), Color(0xFFFFD54F))
            in 12..17 -> CircadianPhaseWidget("Solar Zenith", "☀️", Color(0xFF1565C0), Color(0xFF81D4FA))
            in 18..21 -> CircadianPhaseWidget("Dusk Ritual", "🌙", Color(0xFF6A1B9A), Color(0xFFCE93D8))
            else -> CircadianPhaseWidget("Midnight Watch", "🛡", Color(0xFF263238), Color(0xFFB0BEC5))
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(androidx.glance.color.ColorProvider(day = Color(0xFFF3F4F6), night = Color(0xFF1E1E1E)))
                    .padding(12.dp)
            ) {
                Text(
                    text = "${phase.icon} ${phase.title}",
                    style = TextStyle(
                        color = androidx.glance.color.ColorProvider(day = phase.contentColorLight, night = phase.contentColorDark),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(bottom = 12.dp)
                )

                if (habits.isEmpty()) {
                    Text(
                        text = "The Grimoire is Pristine",
                        style = TextStyle(
                            color = androidx.glance.color.ColorProvider(Color.Gray, Color.Gray),
                            fontSize = 14.sp
                        )
                    )
                } else {
                    LazyColumn {
                        items(habits) { habit ->
                            val isComplete = habit.isCompletedToday
                            val itemBgColorDay = if (isComplete) Color(0xFFE8F5E9) else Color.White
                            val itemBgColorNight = if (isComplete) Color(0xFF1B5E20) else Color(0xFF2C2C2C)
                            val textColorDay = if (isComplete) Color(0xFF2E7D32) else Color.Black
                            val textColorNight = if (isComplete) Color(0xFF69F0AE) else Color.White

                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(androidx.glance.color.ColorProvider(day = itemBgColorDay, night = itemBgColorNight))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.title,
                                    style = TextStyle(
                                        color = androidx.glance.color.ColorProvider(day = textColorDay, night = textColorNight),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = " - ",
                                        style = TextStyle(
                                            color = androidx.glance.color.ColorProvider(day = textColorDay, night = textColorNight),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = GlanceModifier.clickable(actionRunCallback<DecrementHabitActionCallback>(
                                            actionParametersOf(
                                                HabitIdParamKey to habit.id,
                                                RandomKey to java.util.UUID.randomUUID().toString()
                                            )
                                        )).padding(horizontal = 8.dp)
                                    )
                                    Text(
                                        text = "${habit.completedTodayCount}/${habit.targetPerDay}",
                                        style = TextStyle(
                                            color = androidx.glance.color.ColorProvider(day = textColorDay, night = textColorNight),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = " + ",
                                        style = TextStyle(
                                            color = androidx.glance.color.ColorProvider(day = textColorDay, night = textColorNight),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = GlanceModifier.clickable(actionRunCallback<IncrementHabitActionCallback>(
                                            actionParametersOf(
                                                HabitIdParamKey to habit.id,
                                                RandomKey to java.util.UUID.randomUUID().toString()
                                            )
                                        )).padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CircadianPhaseWidget(
    val title: String,
    val icon: String,
    val contentColorLight: Color,
    val contentColorDark: Color
)

val HabitIdParamKey = ActionParameters.Key<Long>("habitId")
val RandomKey = ActionParameters.Key<String>("randomKey")

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
    }
}
