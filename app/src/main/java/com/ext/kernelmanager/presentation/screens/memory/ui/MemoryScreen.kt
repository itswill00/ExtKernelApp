package com.ext.kernelmanager.presentation.screens.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.memory.viewmodel.MemoryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expandedLmk by remember { mutableStateOf(false) }

    // Clear message after 3 seconds
    LaunchedEffect(state.infoMessage) {
        if (state.infoMessage != null) {
            delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manajemen Memori", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Kendalikan RAM & Memori Virtual",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Atur bagaimana sistem mengelola ruang penyimpanan saat menjalankan banyak aplikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else if (!state.isZramSupported) {
                ErrorMessageCard(message = state.infoMessage ?: "Fitur memori tidak didukung perangkat ini.")
            } else {
                // ZRAM Size Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ukuran ZRAM (Memori Virtual)", fontWeight = FontWeight.Bold)
                        Text("Membantu perangkat bernapas saat RAM penuh.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var zramValue by remember { mutableStateOf(state.zramSizeMb.toFloat()) }
                        Slider(
                            value = zramValue,
                            onValueChange = { zramValue = it },
                            valueRange = 0f..4096f,
                            steps = 15,
                            onValueChangeFinished = {
                                viewModel.applyZramSize(zramValue.toInt())
                            }
                        )
                        Text("${zramValue.toInt()} MB", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Swappiness Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kecenderungan Swap (Swappiness)", fontWeight = FontWeight.Bold)
                        Text("Seberapa sering sistem akan menggunakan memori virtual.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var swappinessValue by remember { mutableStateOf(state.swappiness.toFloat()) }
                        Slider(
                            value = swappinessValue,
                            onValueChange = { swappinessValue = it },
                            valueRange = 0f..100f,
                            onValueChangeFinished = {
                                viewModel.applySwappiness(swappinessValue.toInt())
                            }
                        )
                        Text("${swappinessValue.toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LMK Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Manajemen Aplikasi Latar (LMK)", fontWeight = FontWeight.Bold)
                        Text("Pilih seberapa agresif sistem menutup aplikasi yang tidak dipakai.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedLmk,
                            onExpandedChange = { expandedLmk = !expandedLmk }
                        ) {
                            OutlinedTextField(
                                value = state.lmkProfile,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Profil RAM") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLmk) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedLmk,
                                onDismissRequest = { expandedLmk = false }
                            ) {
                                state.lmkProfiles.forEach { profile ->
                                    DropdownMenuItem(
                                        text = { Text(profile) },
                                        onClick = {
                                            viewModel.applyLmkProfile(profile)
                                            expandedLmk = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.infoMessage != null && state.isZramSupported) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.infoMessage!!, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessageCard(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                message,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
