package com.sorcerers.grimoire.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grimoire.model.DummyHabits
import com.example.grimoire.model.HabitEvent
import com.example.grimoire.model.HabitUiModel
import com.example.grimoire.ui.theme.AppThemeMode
import com.example.grimoire.ui.theme.LocalIsDarkTheme

data class CircadianPhase(
    val title: String,
    val icon: String,
    val containerColorLight: Color,
    val contentColorLight: Color,
    val containerColorDark: Color,
    val contentColorDark: Color
)

fun getCircadianPhase(): CircadianPhase {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 4..11 -> CircadianPhase(
            title = "Dawn Vigil",
            icon = "✦",
            containerColorLight = Color(0xFFFFF8E1),
            contentColorLight = Color(0xFFFF8F00),
            containerColorDark = Color(0xFF423719),
            contentColorDark = Color(0xFFFFD54F)
        )
        in 12..17 -> CircadianPhase(
            title = "Solar Zenith",
            icon = "☀️",
            containerColorLight = Color(0xFFE1F5FE),
            contentColorLight = Color(0xFF1565C0),
            containerColorDark = Color(0xFF13364C),
            contentColorDark = Color(0xFF81D4FA)
        )
        in 18..21 -> CircadianPhase(
            title = "Dusk Ritual",
            icon = "🌙",
            containerColorLight = Color(0xFFF3E5F5),
            contentColorLight = Color(0xFF6A1B9A),
            containerColorDark = Color(0xFF331C42),
            contentColorDark = Color(0xFFCE93D8)
        )
        else -> CircadianPhase(
            title = "Midnight Watch",
            icon = "🛡",
            containerColorLight = Color(0xFFECEFF1),
            contentColorLight = Color(0xFF263238),
            containerColorDark = Color(0xFF161E23),
            contentColorDark = Color(0xFFB0BEC5)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    habits: List<HabitUiModel>,
    currentTheme: AppThemeMode = AppThemeMode.DARK,
    onEvent: (HabitEvent) -> Unit
) {
    // Add / Edit Habit Dialog State
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitUiModel?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Sovereign Vault Storage Access Framework (SAF) Launchers
    val exportVaultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { onEvent(HabitEvent.ExportVaultToUri(it.toString())) }
    }

    val importVaultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onEvent(HabitEvent.ImportVaultFromUri(it.toString())) }
    }

    val isDark = LocalIsDarkTheme.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val phase = remember { getCircadianPhase() }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Grimoire",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD4AF37)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) phase.containerColorDark else phase.containerColorLight
                            ) {
                                Text(
                                    text = "${phase.icon} ${phase.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) phase.contentColorDark else phase.contentColorLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    showSettingsDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Chronos Seal Laws") },
                                onClick = {
                                    showMenu = false
                                    showInfoDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info"
                                    )
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Export Vault (JSON)") },
                                onClick = {
                                    showMenu = false
                                    exportVaultLauncher.launch("grimoire_sovereign_vault.json")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = "Export"
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Restore Vault (JSON)") },
                                onClick = {
                                    showMenu = false
                                    importVaultLauncher.launch(arrayOf("application/json"))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Restore"
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    habitToEdit = null
                    showAddDialog = true 
                },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Inscribe") },
                text = { Text("Inscribe Habit", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFFD4AF37),
                contentColor = Color.Black
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Summary Banner
            GrimoireDashboardBanner(habits = habits)

            // Habit List or Empty State
            if (habits.isEmpty()) {
                GrimoireEmptyState(onInscribeClick = { 
                    habitToEdit = null
                    showAddDialog = true 
                })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                ) {
                    items(
                        items = habits,
                        key = { it.id }
                    ) { habit ->
                        HabitCard(
                            habit = habit,
                            onEdit = { 
                                habitToEdit = habit
                                showAddDialog = true 
                            },
                            onIncrement = { onEvent(HabitEvent.IncrementHabit(habit.id)) },
                            onDecrement = { onEvent(HabitEvent.DecrementHabit(habit.id)) },
                            onToggleFreeze = { onEvent(HabitEvent.ToggleFreezeShield(habit.id)) },
                            onDelete = { onEvent(HabitEvent.DeleteHabit(habit.id)) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Habit Dialog
    if (showAddDialog) {
        AddHabitDialog(
            initialHabit = habitToEdit,
            onDismiss = { 
                showAddDialog = false 
                habitToEdit = null
            },
            onInscribe = { title, iconName, colorHex, targetPerDay, reminderTime ->
                if (habitToEdit != null) {
                    onEvent(
                        HabitEvent.EditHabit(
                            habitId = habitToEdit!!.id,
                            title = title,
                            iconName = iconName,
                            colorHex = colorHex,
                            targetPerDay = targetPerDay,
                            reminderTime = reminderTime
                        )
                    )
                } else {
                    onEvent(
                        HabitEvent.AddHabit(
                            title = title,
                            iconName = iconName,
                            colorHex = colorHex,
                            targetPerDay = targetPerDay,
                            reminderTime = reminderTime
                        )
                    )
                }
            }
        )
    }

    // Chronos Seal Rules Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(text = "Chronos Seal Laws", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Midnight Rollover (00:00:00): Every midnight, the temporal daemon verifies daily habit targets.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Streak Progression: Completing your daily target increases your active streak count.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Freeze Shield: Activate a shield to protect your streak on days you cannot fulfill the target. The shield is consumed at midnight to prevent streak loss.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Sovereign Vault: 100% offline local JSON backups ensure your records remain under your sole ownership.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(text = "Understood")
                }
            }
        )
    }

    // Settings Menu Dialog (Theme Sanctuary & Arcane Configurations)
    if (showSettingsDialog) {
        SettingsDialog(
            currentTheme = currentTheme,
            onThemeSelect = { onEvent(HabitEvent.SetThemeMode(it)) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
private fun GrimoireDashboardBanner(habits: List<HabitUiModel>) {
    val completedCount = habits.count { it.isCompletedToday }
    val totalCount = habits.size
    val shieldedCount = habits.count { it.isFrozen }
    val bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BannerStatItem(
                label = "Sealed Today",
                value = "$completedCount/$totalCount",
                icon = Icons.Default.CheckCircle,
                tint = Color(0xFF00E676)
            )

            BannerStatItem(
                label = "Best Streak",
                value = "${bestStreak}d",
                icon = Icons.Default.LocalFireDepartment,
                tint = Color(0xFFFF9800)
            )

            BannerStatItem(
                label = "Shields",
                value = "$shieldedCount",
                icon = Icons.Default.AcUnit,
                tint = Color(0xFF00E5FF)
            )
        }
    }
}

@Composable
private fun BannerStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GrimoireEmptyState(onInscribeClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD4AF37).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The Grimoire is Pristine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No rituals have been bound to the Chronos Seal yet. Inscribe your first daily habit to begin tracking your mastery.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onInscribeClick
            ) {
                Text(
                    text = "Inscribe First Habit",
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(
            habits = DummyHabits.sampleList,
            currentTheme = AppThemeMode.DARK,
            onEvent = {}
        )
    }
}
