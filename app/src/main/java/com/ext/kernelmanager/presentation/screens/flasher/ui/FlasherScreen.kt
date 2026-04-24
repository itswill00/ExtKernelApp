package com.ext.kernelmanager.presentation.screens.flasher.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.flasher.viewmodel.FlasherViewModel
import android.provider.OpenableColumns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlasherScreen(
    viewModel: FlasherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // In a real app, you need to copy the SAF URI to a cache directory 
        // to get a real absolute path for root execution.
        // For simplicity in this demo, we simulate the path resolution.
        uri?.let {
            val fileName = it.lastPathSegment ?: "unknown_file.zip"
            val dummyRealPath = "/sdcard/Download/$fileName" 
            viewModel.onFileSelected(fileName, dummyRealPath)
        }
    }

    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Peringatan Keamanan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Text(
                    "Proses ini akan mengubah sistem inti ponselmu. Pastikan kamu memilih file yang tepat agar ponsel tetap aman.\n\nFile yang dipilih: ${state.selectedFileName}\n\nLanjutkan eksekusi?",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAndFlash() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Saya Yakin")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Batal")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Script & Flasher", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                "Eksekusi Tingkat Lanjut",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Jalankan skrip kustom atau pasang file sistem (.zip, .img, .sh). Harap berhati-hati, fitur ini bisa menyebabkan kerusakan jika digunakan secara asal.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.selectedFileName ?: "Belum ada file yang dipilih",
                        fontWeight = FontWeight.Medium,
                        color = if (state.selectedFileName == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isFlashing
                    ) {
                        Text("Pilih File")
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.requestFlash() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isReadyToFlash && !state.isFlashing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                if (state.isFlashing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onErrorContainer, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sedang Memproses...")
                } else {
                    Text("Eksekusi File", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.logs.isNotEmpty()) {
                Text("Log Sistem Real-time", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        items(state.logs) { log ->
                            Text(log, color = Color(0xFF00FF00), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
