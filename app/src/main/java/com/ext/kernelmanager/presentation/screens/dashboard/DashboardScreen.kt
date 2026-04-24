package com.ext.kernelmanager.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.components.widgets.InstrumentWidget
import com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardViewModel
import com.ext.kernelmanager.presentation.screens.dashboard.widgets.*

/**
 * DashboardScreen: Overhauled to use a dynamic Widget Framework.
 * No longer hardcoded. Efficiently renders only discovered hardware instruments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 1. Dynamic Widget Assembly
    val widgets = remember(state.telemetry) {
        val list = mutableListOf<InstrumentWidget>()
        state.telemetry?.let { t ->
            // Mandatory Identity
            list.add(IdentityWidget(t.kernel, state.isRooted, t.uptime))
            
            // Performance Layer
            list.add(CpuLoadWidget(t.cpu))
            
            // Snapshot Grid
            list.add(SummaryGridWidget(listOf(
                SummaryStat("GPU CLOCK", t.gpu.currentFreq, Icons.Default.Settings, Color(0xFF4DB6AC)),
                SummaryStat("BATT LEVEL", "${t.battery.percentage}%", Icons.Default.Favorite, Color(0xFFEF9A9A)),
                SummaryStat("VOLTAGE", "%.2f V".format(t.battery.voltage), Icons.Default.Refresh, Color(0xFF90A4AE)),
                SummaryStat("TEMP", "${t.battery.temperature.toInt()}°C", Icons.Default.Settings, Color(0xFFFFAB91))
            )))
        }
        list.sortedBy { it.priority }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("System Dashboard", fontWeight = FontWeight.Black)
                        Text("Active Telemetry Engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // High-efficiency dynamic rendering
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
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
