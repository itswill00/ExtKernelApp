package com.ext.kernelmanager

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ext.kernelmanager.core.engine.MasterKernelEngine
import com.ext.kernelmanager.core.root.ui.RootRequestScreen
import com.ext.kernelmanager.presentation.MainShell
import com.ext.kernelmanager.presentation.theme.ExtKernelTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var kernelEngine: MasterKernelEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        val didCrash = prefs.getBoolean("did_crash", false)
        
        setContent {
            ExtKernelTheme {
                val scope = rememberCoroutineScope()
                var rootStatus by remember { mutableStateOf<RootStatus>(RootStatus.Checking) }
                val showCrashDialog = remember { mutableStateOf(didCrash) }

                // Check root on app open
                LaunchedEffect(Unit) {
                    val hasRoot = kernelEngine.checkAndRequestRoot()
                    rootStatus = if (hasRoot) RootStatus.Granted else RootStatus.Denied
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (rootStatus) {
                        RootStatus.Checking -> {
                            RootRequestScreen(isChecking = true, onRetry = {})
                        }
                        RootStatus.Denied -> {
                            RootRequestScreen(
                                isChecking = false,
                                onRetry = {
                                    rootStatus = RootStatus.Checking
                                    scope.launch {
                                        val hasRoot = kernelEngine.checkAndRequestRoot()
                                        rootStatus = if (hasRoot) RootStatus.Granted else RootStatus.Denied
                                    }
                                }
                            )
                        }
                        RootStatus.Granted -> {
                            if (showCrashDialog.value) {
                                CrashDialog(
                                    onReset = {
                                        prefs.edit().putBoolean("did_crash", false).apply()
                                        showCrashDialog.value = false
                                    },
                                    onIgnore = {
                                        prefs.edit().putBoolean("did_crash", false).apply()
                                        showCrashDialog.value = false
                                    }
                                )
                            } else {
                                MainShell()
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class RootStatus { Checking, Granted, Denied }

@Composable
fun CrashDialog(onReset: () -> Unit, onIgnore: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("We are sorry") },
        text = { Text("The application encountered a serious issue during the previous session. This might be caused by settings that are incompatible with your system. Would you like to reset all settings to prevent further issues?") },
        confirmButton = {
            Button(onClick = onReset) { Text("Reset Settings") }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) { Text("Ignore") }
        }
    )
}
