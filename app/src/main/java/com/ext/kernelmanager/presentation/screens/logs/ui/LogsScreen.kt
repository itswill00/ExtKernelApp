package com.ext.kernelmanager.presentation.screens.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.logs.viewmodel.LogsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom
    LaunchedEffect(state.kernelLogs.size, state.systemLogs.size) {
        val size = if (state.isKernelLogActive) state.kernelLogs.size else state.systemLogs.size
        if (size > 0) {
            listState.animateScrollToItem(size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Live Kernel & System Logs", fontWeight = FontWeight.Bold) })
                TabRow(selectedTabIndex = if (state.isKernelLogActive) 0 else 1) {
                    Tab(
                        selected = state.isKernelLogActive,
                        onClick = { viewModel.startKernelLog() },
                        text = { Text("Kernel (dmesg)") }
                    )
                    Tab(
                        selected = !state.isKernelLogActive,
                        onClick = { viewModel.startSystemLog() },
                        text = { Text("System (logcat)") }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                val currentLogs = if (state.isKernelLogActive) state.kernelLogs else state.systemLogs
                items(currentLogs) { log ->
                    Text(
                        text = log,
                        color = if (log.contains("E/", ignoreCase = true) || log.contains("error", ignoreCase = true)) 
                            Color.Red else Color(0xFF00FF00),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
