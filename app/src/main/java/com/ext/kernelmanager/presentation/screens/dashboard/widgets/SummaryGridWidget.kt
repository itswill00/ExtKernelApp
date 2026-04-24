package com.ext.kernelmanager.presentation.screens.dashboard.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ext.kernelmanager.presentation.components.widgets.InstrumentWidget

/**
 * SummaryGridWidget: A high-efficiency grid for atomic hardware metrics.
 */
class SummaryGridWidget(
    private val stats: List<SummaryStat>
) : InstrumentWidget {
    override val title: String = "Status Snapshot"
    override val priority: Int = 20

    @Composable
    override fun Render(modifier: Modifier) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val pairs = stats.chunked(2)
            pairs.forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { stat ->
                        StatBox(
                            modifier = Modifier.weight(1f),
                            stat = stat
                        )
                    }
                    if (pair.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    @Composable
    private fun StatBox(modifier: Modifier, stat: SummaryStat) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Icon(stat.icon, null, modifier = Modifier.size(20.dp), tint = stat.color)
                Spacer(modifier = Modifier.height(12.dp))
                Text(stat.label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
        }
    }
}

data class SummaryStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
