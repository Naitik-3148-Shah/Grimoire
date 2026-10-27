package com.sorcerers.grimoire.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChronosHeatmap(
    history: List<Boolean>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val completedCount = history.count { it }
    val displayList = if (history.size >= 30) {
        history.takeLast(30)
    } else {
        List(30 - history.size) { false } + history
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chronos Trail (30 Days)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completedCount / 30 sealed",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 11.sp
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 15
        ) {
            displayList.forEachIndexed { index, isMet ->
                val daysAgo = 29 - index
                val date = java.time.LocalDate.now().minusDays(daysAgo.toLong())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
                val dateString = date.format(formatter)
                
                val cellColor = if (isMet) {
                    accentColor
                } else {
                    if (isSystemInDarkTheme()) Color(0xFF2D2D2D) else Color(0xFFEBEDF0)
                }
                
                var showTooltip by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(cellColor, RoundedCornerShape(3.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showTooltip = true
                                }
                            )
                        }
                ) {
                    if (showTooltip) {
                        androidx.compose.ui.window.Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { showTooltip = false }
                        ) {
                            Box(
                                modifier = Modifier.padding(bottom = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = dateString,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        LaunchedEffect(showTooltip) {
                            delay(2000)
                            showTooltip = false
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "30d ago",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}
