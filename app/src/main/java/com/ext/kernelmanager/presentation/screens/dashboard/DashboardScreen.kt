package com.ext.kernelmanager.presentation.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.domain.model.telemetry.*
import com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Ext Kernel", fontWeight = FontWeight.Black)
                        Text("System Command Center", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val telemetry = state.telemetry!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IdentityCard(telemetry.kernel, state.isRooted, telemetry.uptime)
                CpuLoadInstrument(telemetry.cpu)
                HardwareGrid(telemetry)
                MemoryMapInstrument(telemetry.memory)
                ThermalMappingInstrument(telemetry.thermal)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun IdentityCard(kernel: KernelTelemetry, isRooted: Boolean, uptime: String) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = (if (isRooted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (isRooted) Icons.Default.CheckCircle else Icons.Default.Info, 
                        contentDescription = null, 
                        modifier = Modifier.padding(8.dp).size(24.dp),
                        tint = if (isRooted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Privileged Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (isRooted) "Superuser access verified" else "Limited functionality", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("KERNEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(kernel.version, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoLabel("COMPILER", kernel.compiler)
                InfoLabel("UPTIME", uptime)
            }
        }
    }
}

@Composable
fun CpuLoadInstrument(cpu: CpuTelemetry) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("PROCESSOR LOAD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                cpu.cores.forEach { core ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(core.load / 100f)
                                    .align(Alignment.BottomCenter)
                                    .background(if (core.isOnline) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(4.dp))
                            )
                        }
                        Text(core.id.toString(), fontSize = 8.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            cpu.clusters.forEach { cluster ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cluster ${cluster.id}: ${cluster.governor}", style = MaterialTheme.typography.labelSmall)
                    Text("${cluster.minFreq} - ${cluster.maxFreq}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun HardwareGrid(telemetry: SystemTelemetry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatInstrument(Modifier.weight(1f), "GPU", telemetry.gpu.currentFreq, Icons.Default.Settings, MaterialTheme.colorScheme.secondaryContainer)
            StatInstrument(Modifier.weight(1f), "BATT", "${telemetry.battery.percentage}%", Icons.Default.Favorite, MaterialTheme.colorScheme.tertiaryContainer)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatInstrument(Modifier.weight(1f), "VLTG", "%.2f V".format(telemetry.battery.voltage), Icons.Default.Settings, MaterialTheme.colorScheme.primaryContainer)
            StatInstrument(Modifier.weight(1f), "CURR", "${telemetry.battery.current} mA", Icons.Default.Refresh, MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
fun MemoryMapInstrument(mem: MemoryTelemetry) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("MEMORY ARCHITECTURE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = mem.usagePercent,
                modifier = Modifier.fillMaxWidth().height(12.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(12.dp))
            MemoryRow("Total Physical", "${mem.total} MB")
            MemoryRow("Available", "${mem.available} MB")
            MemoryRow("ZRAM Size", "${mem.zramSize} MB")
            MemoryRow("Swap Free", "${mem.swapFree} MB")
        }
    }
}

@Composable
fun ThermalMappingInstrument(thermal: List<ThermalTelemetry>) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("THERMAL SENSORS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            thermal.take(6).forEach { sensor ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sensor.type.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    Text("${sensor.temperature.toInt()}°C", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (sensor.temperature > 45) Color.Red else MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun StatInstrument(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun InfoLabel(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MemoryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
