package com.ext.kernelmanager.presentation.screens.explorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ExplorerState(
    val currentPath: String = "/sys",
    val files: List<FileItem> = emptyList(),
    val fileContent: String? = null,
    val isLoading: Boolean = false,
    val infoMessage: String? = null
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean
)

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val tuningRepository: HardcoreTuningRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExplorerState())
    val state: StateFlow<ExplorerState> = _state.asStateFlow()

    init {
        navigateTo(_state.value.currentPath)
    }

    fun navigateTo(path: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, fileContent = null)
            val dir = File(path)
            if (dir.isDirectory) {
                val list = dir.listFiles()?.map { 
                    FileItem(it.name, it.absolutePath, it.isDirectory)
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
                
                _state.value = _state.value.copy(
                    currentPath = path,
                    files = list,
                    isLoading = false
                )
            } else {
                val content = tuningRepository.readSysfs(path)
                _state.value = _state.value.copy(
                    currentPath = path,
                    fileContent = content,
                    isLoading = false
                )
            }
        }
    }

    fun writeValue(path: String, value: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = tuningRepository.writeSysfs(path, value)
            if (success) {
                val newContent = tuningRepository.readSysfs(path)
                _state.value = _state.value.copy(
                    fileContent = newContent,
                    infoMessage = "Successfully wrote to sysfs."
                )
            } else {
                _state.value = _state.value.copy(infoMessage = "Write failed. Ensure the path is writable.")
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun goBack() {
        val parent = File(_state.value.currentPath).parent ?: return
        navigateTo(parent)
    }
    
    fun clearMessage() {
        _state.value = _state.value.copy(infoMessage = null)
    }
}
