package com.ext.kernelmanager.presentation.screens.cpu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.CpuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CpuTuningState(
    val currentGovernor: String = "Memuat...",
    val availableGovernors: List<String> = emptyList(),
    val isSupported: Boolean = true,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CpuTuningViewModel @Inject constructor(
    private val cpuRepository: CpuRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CpuTuningState())
    val state: StateFlow<CpuTuningState> = _state.asStateFlow()

    init {
        refreshCpuData()
    }

    fun refreshCpuData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val current = cpuRepository.getCurrentGovernor()
                val available = cpuRepository.getAvailableGovernors()
                
                if (available.isEmpty()) {
                    _state.value = _state.value.copy(
                        isSupported = false,
                        errorMessage = "Sistem perangkat ini mengunci pengaturan performa. Fitur ini sementara dinonaktifkan.",
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        currentGovernor = current,
                        availableGovernors = available,
                        isSupported = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSupported = false,
                    errorMessage = "Terjadi kendala saat membaca data sistem.",
                    isLoading = false
                )
            }
        }
    }

    fun updateGovernor(newGovernor: String) {
        viewModelScope.launch {
            val success = cpuRepository.setGovernor(newGovernor)
            if (success) {
                _state.value = _state.value.copy(currentGovernor = newGovernor)
            } else {
                _state.value = _state.value.copy(
                    errorMessage = "Gagal mengubah mode performa. Izin sistem mungkin ditolak."
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
