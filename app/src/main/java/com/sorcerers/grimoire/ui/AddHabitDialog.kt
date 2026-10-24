package com.sorcerers.grimoire.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.grimoire.model.HabitUiModel

private val AVAILABLE_ICONS = listOf(
    "book" to "Study",
    "water" to "Hydration",
    "fire" to "Vitality",
    "fitness" to "Training",
    "meditation" to "Mind",
    "code" to "Craft",
    "sleep" to "Rest",
    "run" to "Movement",
    "shield" to "Defense"
)

private val ARCANE_PALETTE = listOf(
    "#D4AF37", // Arcane Gold
    "#29B6F6", // Mana Cyan
    "#00E676", // Alchemic Emerald
    "#AB47BC", // Mystic Amethyst
    "#FF5252", // Dragon Crimson
    "#FFB300", // Solar Amber
    "#7C4DFF", // Void Violet
    "#26A69A"  // Runic Teal
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitDialog(
    initialHabit: HabitUiModel? = null,
    onDismiss: () -> Unit,
    onInscribe: (title: String, iconName: String, colorHex: String, targetPerDay: Int, reminderTime: String?) -> Unit
) {
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: "book") }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorHex ?: "#D4AF37") }
    var targetPerDay by remember { mutableIntStateOf(initialHabit?.targetPerDay ?: 1) }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "") }
    var titleError by remember { mutableStateOf(false) }

    val activeColor = remember(selectedColor) { parseHexColor(selectedColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (initialHabit == null) "Inscribe New Habit" else "Edit Habit", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Habit Title") },
                    placeholder = { Text("e.g. Master Kotlin Sorcery") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text(text = "Title cannot be empty", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Per Day Stepper
                Text(
                    text = "Daily Target: $targetPerDay time${if (targetPerDay > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { if (targetPerDay > 1) targetPerDay-- },
                        enabled = targetPerDay > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Target")
                    }

                    Text(
                        text = "$targetPerDay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FilledTonalIconButton(
                        onClick = { if (targetPerDay < 99) targetPerDay++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Target")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Icon
                Text(
                    text = "Select Sigil",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVAILABLE_ICONS.forEach { (iconKey, _) ->
                        val isSelected = selectedIcon == iconKey
                        val iconVector = getHabitIcon(iconKey)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else null,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedIcon = iconKey }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arcane Color Palette
                Text(
                    text = "Arcane Resonance Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ARCANE_PALETTE.forEach { hex ->
                        val color = parseHexColor(hex)
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Time (Optional)
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder (Optional)") },
                    placeholder = { Text("e.g. 09:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onInscribe(
                            title.trim(),
                            selectedIcon,
                            selectedColor,
                            targetPerDay,
                            reminderTime.trim().ifEmpty { null }
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeColor, contentColor = Color.Black)
            ) {
                Text(text = if (initialHabit == null) "Inscribe Seal" else "Update Seal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
