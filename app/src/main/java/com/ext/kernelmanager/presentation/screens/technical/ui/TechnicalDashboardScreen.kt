package com.ext.kernelmanager.presentation.screens.technical.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.technical.viewmodel.CpuClusterState
import com.ext.kernelmanager.presentation.screens.technical.viewmodel.GpuState
import com.ext.kernelmanager.presentation.screens.technical.viewmodel.TechnicalDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalDashboardScreen(
    viewModel: TechnicalDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Instrumentation", fontWeight = FontWeight.Bold) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "PROCESSOR CLUSTERS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                state.clusters.forEach { cluster ->
                    ClusterInstrument(cluster)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "GRAPHICS ENGINE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                state.gpu?.let { gpu ->
                    GpuInstrument(gpu)
                } ?: Text("No GPU engine data available.", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ClusterInstrument(cluster: CpuClusterState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Cluster ${cluster.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${cluster.currentFreq / 1000} MHz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val load = if (cluster.maxFreq > 0) cluster.currentFreq.toFloat() / cluster.maxFreq.toFloat() else 0f
            val animatedLoad by animateFloatAsState(targetValue = load)
            
            LinearProgressIndicator(
                progress = animatedLoad,
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Governor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(cluster.governor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Operational Range", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        "${cluster.minFreq / 1000} - ${cluster.maxFreq / 1000} MHz",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GpuInstrument(gpu: GpuState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DeveloperBoard, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "GPU Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${gpu.currentFreq / 1000000} MHz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val load = if (gpu.maxFreq > 0) gpu.currentFreq.toFloat() / gpu.maxFreq.toFloat() else 0f
            val animatedLoad by animateFloatAsState(targetValue = load)
            
            LinearProgressIndicator(
                progress = animatedLoad,
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.secondary,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Governor: ${gpu.governor}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
