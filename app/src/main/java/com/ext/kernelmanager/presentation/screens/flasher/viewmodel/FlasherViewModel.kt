package com.ext.kernelmanager.presentation.screens.flasher.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.FlasherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlasherState(
    val selectedFileName: String? = null,
    val selectedFilePath: String? = null, // Real path needed for root shell
    val isReadyToFlash: Boolean = false,
    val logs: List<String> = emptyList(),
    val isFlashing: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FlasherViewModel @Inject constructor(
    private val flasherRepository: FlasherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FlasherState())
    val state: StateFlow<FlasherState> = _state.asStateFlow()

    fun onFileSelected(fileName: String, realPath: String) {
        viewModelScope.launch {
            val isValid = flasherRepository.validateFile(realPath)
            if (isValid) {
                _state.value = _state.value.copy(
                    selectedFileName = fileName,
                    selectedFilePath = realPath,
                    isReadyToFlash = true,
                    errorMessage = null
                )
            } else {
                _state.value = _state.value.copy(
                    isReadyToFlash = false,
                    errorMessage = "File tidak valid atau terlalu besar. Hanya mendukung .sh, .img, atau .zip (maksimal 150MB)."
                )
            }
        }
    }

    fun requestFlash() {
        if (_state.value.isReadyToFlash) {
            _state.value = _state.value.copy(showConfirmDialog = true)
        }
    }

    fun dismissConfirmDialog() {
        _state.value = _state.value.copy(showConfirmDialog = false)
    }

    fun confirmAndFlash() {
        val path = _state.value.selectedFilePath ?: return
        
        _state.value = _state.value.copy(
            showConfirmDialog = false,
            isFlashing = true,
            logs = listOf("Menyiapkan proses eksekusi...")
        )

        viewModelScope.launch {
            // Simple generic execution mapping. In real-world enterprise apps:
            // - .zip -> magisk --install-module path
            // - .img -> dd if=path of=/dev/block/... (Requires detecting partition!)
            // - .sh -> sh path
            // Here we use safe representations.
            
            val command = when {
                path.endsWith(".sh") -> "sh \"$path\""
                path.endsWith(".zip") -> "magisk --install-module \"$path\"" // Assuming Magisk environment
                else -> "echo 'Flash .img membutuhkan deteksi partisi. Simulasi flash dimulai...'; sleep 2; echo 'Simulasi sukses.'"
            }

            flasherRepository.executeWithProgress(command).collect { logLine ->
                val currentLogs = _state.value.logs.toMutableList()
                currentLogs.add(logLine)
                _state.value = _state.value.copy(logs = currentLogs)
            }
            
            _state.value = _state.value.copy(isFlashing = false)
        }
    }
}
