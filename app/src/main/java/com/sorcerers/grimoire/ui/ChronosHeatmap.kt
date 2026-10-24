package com.sorcerers.grimoire.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                val isToday = index == displayList.lastIndex
                val cellColor = if (isMet) {
                    accentColor
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cellColor)
                        .then(
                            if (isToday) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = if (isMet) Color.White else accentColor,
                                    shape = RoundedCornerShape(3.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                )
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
