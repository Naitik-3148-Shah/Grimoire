package com.sorcerers.grimoire.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grimoire.model.HabitUiModel

fun parseHexColor(hex: String, fallback: Color = Color(0xFFD4AF37)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

fun getHabitIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "book", "study", "read" -> Icons.Default.MenuBook
        "water", "hydration", "drink" -> Icons.Default.WaterDrop
        "fire", "flame" -> Icons.Default.LocalFireDepartment
        "fitness", "gym", "workout" -> Icons.Default.FitnessCenter
        "meditation", "mind", "zen" -> Icons.Default.SelfImprovement
        "code", "dev", "hack" -> Icons.Default.Code
        "sleep", "rest" -> Icons.Default.Bedtime
        "run", "walk", "jog" -> Icons.Default.DirectionsRun
        "shield", "guard" -> Icons.Default.Shield
        else -> Icons.Default.Star
    }
}

@Composable
fun HabitCard(
    habit: HabitUiModel,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleFreeze: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(habit.colorHex) { parseHexColor(habit.colorHex) }
    val habitIcon = remember(habit.iconName) { getHabitIcon(habit.iconName) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = habit.completionProgress,
        label = "habitProgressAnimation"
    )

    val shieldBgColor by animateColorAsState(
        targetValue = if (habit.isFrozen) Color(0xFF00E5FF).copy(alpha = 0.18f) else Color.Transparent,
        label = "shieldBg"
    )
    val shieldBorderColor by animateColorAsState(
        targetValue = if (habit.isFrozen) Color(0xFF00E5FF) else MaterialTheme.colorScheme.outlineVariant,
        label = "shieldBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon, Title, Reminder, and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habitIcon,
                        contentDescription = habit.title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (habit.reminderTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Reminder",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = habit.reminderTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Streak & Freeze Shield Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.currentStreak}d Streak",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFB74D)
                        )
                        if (habit.longestStreak > habit.currentStreak) {
                            Text(
                                text = " (Best: ${habit.longestStreak})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Freeze Shield Toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = shieldBgColor,
                    border = BorderStroke(1.dp, shieldBorderColor),
                    modifier = Modifier.clickable { onToggleFreeze() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (habit.isFrozen) Icons.Default.AcUnit else Icons.Outlined.Shield,
                            contentDescription = "Freeze Shield",
                            tint = if (habit.isFrozen) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (habit.isFrozen) "Shield Active" else "Shield Offline",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (habit.isFrozen) FontWeight.Bold else FontWeight.Normal,
                            color = if (habit.isFrozen) Color(0xFF80D8FF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar and Counter Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${habit.completedTodayCount} / ${habit.targetPerDay} today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (habit.isCompletedToday) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E676).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Sealed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Increment / Decrement Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = habit.completedTodayCount > 0,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrement",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    FilledTonalIconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increment",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(12.dp))

            // 30-Day Chronos Heatmap Trail
            ChronosHeatmap(
                history = habit.last30DaysHistory,
                accentColor = accentColor
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Dismiss Habit Seal") },
            text = { Text(text = "Are you sure you want to permanently erase \"${habit.title}\" and all of its Chronos logs?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text(text = "Erase", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
