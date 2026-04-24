package com.ext.kernelmanager.presentation.screens.technical.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardcore System Monitor", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Green)
                }
            } else {
                Text("CPU CLUSTERS", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                state.clusters.forEach { cluster ->
                    ClusterCard(cluster, onGovChange = { viewModel.updateClusterGovernor(cluster.id, it) })
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("GPU STATUS", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                state.gpu?.let { gpu ->
                    GpuCard(gpu, onGovChange = { viewModel.updateGpuGovernor(it) })
                } ?: Text("GPU not detected or unsupported.", color = Color.Gray, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ClusterCard(cluster: CpuClusterState, onGovChange: (String) -> Unit) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cluster ${cluster.id}", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${cluster.currentFreq / 1000} MHz", color = Color.Green, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Minimalist Linear Progress for Load (Simulated with current/max)
            val load = if (cluster.maxFreq > 0) cluster.currentFreq.toFloat() / cluster.maxFreq.toFloat() else 0f
            LinearProgressIndicator(
                progress = load,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color.Green,
                trackColor = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Governor: ", fontSize = 12.sp, color = Color.Gray)
                Text(cluster.governor, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            // Compact Range Info
            Text(
                "Range: ${cluster.minFreq / 1000} - ${cluster.maxFreq / 1000} MHz",
                fontSize = 10.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun GpuCard(gpu: GpuState, onGovChange: (String) -> Unit) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GPU Engine", fontWeight = FontWeight.Bold, color = Color.White)
                Text("${gpu.currentFreq / 1000000} MHz", color = Color.Cyan, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val load = if (gpu.maxFreq > 0) gpu.currentFreq.toFloat() / gpu.maxFreq.toFloat() else 0f
            LinearProgressIndicator(
                progress = load,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color.Cyan,
                trackColor = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Governor: ${gpu.governor}", fontSize = 12.sp, color = Color.White)
        }
    }
}
