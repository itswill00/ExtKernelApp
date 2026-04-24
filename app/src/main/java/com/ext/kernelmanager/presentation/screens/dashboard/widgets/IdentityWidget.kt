package com.ext.kernelmanager.presentation.screens.dashboard.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ext.kernelmanager.domain.model.telemetry.KernelTelemetry
import com.ext.kernelmanager.presentation.components.widgets.InstrumentWidget

/**
 * IdentityWidget: Identity instrument for kernel and system status.
 */
class IdentityWidget(
    private val kernel: KernelTelemetry,
    private val isRooted: Boolean,
    private val uptime: String
) : InstrumentWidget {
    override val title: String = "Identity"
    override val priority: Int = 0

    @Composable
    override fun Render(modifier: Modifier) {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = (if (isRooted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null, 
                            modifier = Modifier.padding(8.dp).size(24.dp),
                            tint = if (isRooted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("System Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (isRooted) "Privileged session active" else "Limited access", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("LINUX KERNEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(kernel.version, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LabelValue("ARCHITECTURE", kernel.architecture)
                    LabelValue("UPTIME", uptime)
                }
            }
        }
    }

    @Composable
    private fun LabelValue(label: String, value: String) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}
