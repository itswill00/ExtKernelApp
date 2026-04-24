package com.ext.kernelmanager.core.root.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RootRequestScreen(
    isChecking: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (isChecking) Icons.Default.Lock else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isChecking) Color.Gray else Color.Red,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isChecking) "Memvalidasi Izin Sistem..." else "Akses Root Ditolak",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isChecking) "Mohon tunggu sebentar, kami sedang memastikan aplikasi memiliki izin yang cukup untuk mengelola kernel Anda." 
                       else "Ext Kernel Manager memerlukan hak akses Root untuk memodifikasi sistem. Tanpa izin ini, aplikasi tidak dapat berfungsi.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            if (!isChecking) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Coba Lagi", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            }
        }
    }
}
