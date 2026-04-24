package com.ext.kernelmanager.presentation.screens.dashboard.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ext.kernelmanager.domain.model.telemetry.CpuTelemetry
import com.ext.kernelmanager.presentation.components.widgets.InstrumentWidget

/**
 * CpuLoadWidget: Visualizes individual core load in a standardized instrument format.
 */
class CpuLoadWidget(private val cpuData: CpuTelemetry) : InstrumentWidget {
    override val title: String = "Processor Load"
    override val priority: Int = 10

    @Composable
    override fun Render(modifier: Modifier) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Efficient Grid for Cores
                val coreGroups = cpuData.cores.chunked(4)
                coreGroups.forEach { group ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        group.forEach { core ->
                            CorePillar(
                                modifier = Modifier.weight(1f),
                                id = core.id,
                                load = core.load,
                                isOnline = core.isOnline
                            )
                        }
                        // Fill empty space if group is less than 4
                        if (group.size < 4) {
                            repeat(4 - group.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CorePillar(modifier: Modifier, id: Int, load: Int, isOnline: Boolean) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(load / 100f)
                        .align(Alignment.BottomCenter)
                        .background(
                            if (isOnline) MaterialTheme.colorScheme.primary 
                            else Color.Gray.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(id.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
