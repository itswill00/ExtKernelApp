package com.ext.kernelmanager.presentation.screens.dashboard

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // TOP HEADER: Hardware Identity
        HardwareHeader(
            model = state.deviceIdentity?.model ?: "Unknown Device",
            kernel = state.deviceIdentity?.kernel ?: "Generic Linux"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // GRID: Core Statistics
        Text("SYSTEM TOPOLOGY", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        StatGrid(state)

        Spacer(modifier = Modifier.height(24.dp))

        // MEMORY INSTRUMENT
        Text("MEMORY FOOTPRINT", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        MemoryInstrument(state)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun HardwareHeader(model: String, kernel: String) {
    Column {
        Text(
            text = model.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Text(
            text = "KERNEL: $kernel",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StatGrid(state: com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardState) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatBox(
            modifier = Modifier.weight(1f),
            label = "CPU CLOCK",
            value = state.cpuFreq,
            color = Color(0xFF64B5F6)
        )
        Spacer(modifier = Modifier.width(12.dp))
        StatBox(
            modifier = Modifier.weight(1f),
            label = "THERMAL",
            value = state.temperature,
            color = if (state.temperature.contains("N/A")) Color.Gray else Color(0xFFFF8A65)
        )
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(
        modifier = modifier,
        color = Color(0xFF111111),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun MemoryInstrument(state: com.ext.kernelmanager.presentation.screens.dashboard.viewmodel.DashboardState) {
    Surface(
        color = Color(0xFF111111),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VIRTUAL MEMORY (ZRAM)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("${(state.ramUsagePercent * 100).toInt()}%", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = state.ramUsagePercent,
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.ramText,
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 16.sp
            )
        }
    }
}
