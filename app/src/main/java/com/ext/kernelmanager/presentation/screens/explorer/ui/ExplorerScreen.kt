package com.ext.kernelmanager.presentation.screens.explorer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ext.kernelmanager.presentation.screens.explorer.viewmodel.ExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Sysfs Explorer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(state.currentPath, fontSize = 10.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (state.fileContent != null) {
                FileEditor(
                    path = state.currentPath,
                    content = state.fileContent!!,
                    onSave = { viewModel.writeValue(state.currentPath, it) }
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.files) { item ->
                        ListItem(
                            headlineContent = { Text(item.name, fontSize = 14.sp) },
                            leadingContent = { 
                                Icon(
                                    if (item.isDirectory) Icons.Default.List else Icons.Default.Build, 
                                    contentDescription = null,
                                    tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else Color.Gray
                                ) 
                            },
                            trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { viewModel.navigateTo(item.path) }
                        )
                        Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun FileEditor(path: String, content: String, onSave: (String) -> Unit) {
    var textValue by remember { mutableStateOf(content) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Editing: ${path.substringAfterLast("/")}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Current Value") },
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onSave(textValue) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simpan Perubahan")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hati-hati: Menulis nilai yang salah dapat menyebabkan sistem crash.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
