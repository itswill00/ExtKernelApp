package com.ext.kernelmanager.presentation.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.components.widgets.InstrumentWidget
import com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardViewModel
import com.ext.kernelmanager.presentation.screens.dashboard.widgets.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widgets = remember(state.telemetry) {
        val list = mutableListOf<InstrumentWidget>()
        state.telemetry?.let { t ->
            list.add(IdentityWidget(t.kernel, state.isRooted, t.uptime))
            list.add(CpuLoadWidget(t.cpu))
            list.add(SummaryGridWidget(listOf(
                SummaryStat("Graphics", t.gpu.currentFreq, Icons.Default.Settings, Color(0xFF4DB6AC)),
                SummaryStat("Battery", "${t.battery.percentage}%", Icons.Default.Favorite, Color(0xFFEF9A9A)),
                SummaryStat("Voltage", "%.2f V".format(t.battery.voltage), Icons.Default.Settings, Color(0xFF90A4AE)),
                SummaryStat("Internal", "${t.battery.temperature.toInt()}°C", Icons.Default.Settings, Color(0xFFFFAB91))
            )))
            list.add(MemoryMapWidget(t.memory))
        }
        list.sortedBy { it.priority }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Dashboard", fontWeight = FontWeight.Bold)
                        Text("Live system analytics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(widgets) { widget ->
                    widget.Render(modifier = Modifier.fillMaxWidth())
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

class MemoryMapWidget(private val mem: com.ext.kernelmanager.domain.model.telemetry.MemoryTelemetry) : InstrumentWidget {
    override val title: String = "Memory"
    override val priority: Int = 30

    @Composable
    override fun Render(modifier: Modifier) {
        ElevatedCard(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                
                val usagePercent by animateFloatAsState(targetValue = mem.usagePercent)
                LinearProgressIndicator(
                    progress = usagePercent,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("Total", "${mem.total} MB")
                    Metric("Used", "${(mem.total - mem.available)} MB")
                    Metric("Cached", "${mem.cached} MB")
                }
            }
        }
    }

    @Composable
    private fun Metric(label: String, value: String) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}
