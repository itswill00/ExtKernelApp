package com.ext.kernelmanager.presentation.screens.logs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.KernelLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsState(
    val kernelLogs: List<String> = emptyList(),
    val systemLogs: List<String> = emptyList(),
    val isKernelLogActive: Boolean = true
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val kernelLogRepository: KernelLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogsState())
    val state: StateFlow<LogsState> = _state.asStateFlow()

    private var kernelLogJob: Job? = null
    private var systemLogJob: Job? = null

    init {
        startKernelLog()
    }

    fun startKernelLog() {
        systemLogJob?.cancel()
        kernelLogJob?.cancel()
        _state.value = _state.value.copy(isKernelLogActive = true, kernelLogs = emptyList())
        
        kernelLogJob = viewModelScope.launch {
            kernelLogRepository.getKernelLogs().collect { log ->
                val current = _state.value.kernelLogs.toMutableList()
                if (current.size > 500) current.removeAt(0) // Limit size
                current.add(log)
                _state.value = _state.value.copy(kernelLogs = current)
            }
        }
    }

    fun startSystemLog() {
        kernelLogJob?.cancel()
        systemLogJob?.cancel()
        _state.value = _state.value.copy(isKernelLogActive = false, systemLogs = emptyList())
        
        systemLogJob = viewModelScope.launch {
            kernelLogRepository.getSystemLogs().collect { log ->
                val current = _state.value.systemLogs.toMutableList()
                if (current.size > 500) current.removeAt(0)
                current.add(log)
                _state.value = _state.value.copy(systemLogs = current)
            }
        }
    }
}
