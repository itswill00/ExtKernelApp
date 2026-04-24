package com.ext.kernelmanager.presentation.screens.battery.ui

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
import com.ext.kernelmanager.presentation.screens.battery.viewmodel.BatteryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Clear message after 4 seconds
    LaunchedEffect(state.infoMessage) {
        if (state.infoMessage != null && !state.isLoading) {
            delay(4000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Baterai & Suhu", fontWeight = FontWeight.Bold) })
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
                "Profil Sistem Cerdas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Pilih satu profil yang paling sesuai dengan kebutuhan Anda saat ini. Kami akan mengatur sisa parameter di balik layar.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            ProfileCard(
                title = "Battery Saver",
                desc = "Menghemat daya baterai. Cocok saat Anda sedang jauh dari pengisi daya.",
                isSelected = state.currentProfile == "Battery Saver",
                onClick = { viewModel.applyProfile("Battery Saver") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                title = "Balanced",
                desc = "Keseimbangan sempurna antara daya tahan baterai dan performa. Direkomendasikan untuk harian.",
                isSelected = state.currentProfile == "Balanced",
                onClick = { viewModel.applyProfile("Balanced") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                title = "Performance",
                desc = "Membuka seluruh potensi kecepatan prosesor. Cocok untuk bermain game (Mungkin baterai lebih cepat habis dan sedikit hangat).",
                isSelected = state.currentProfile == "Performance",
                onClick = { viewModel.applyProfile("Performance") }
            )

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(state.infoMessage ?: "Menerapkan profil...", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            } else if (state.infoMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.infoMessage!!, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(title: String, desc: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isSelected, onClick = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, fontSize = 13.sp, lineHeight = 18.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray)
        }
    }
}
