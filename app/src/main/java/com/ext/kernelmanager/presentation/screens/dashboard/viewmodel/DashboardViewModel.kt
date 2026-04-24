package com.ext.kernelmanager.presentation.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.core.hardware.DeviceIdentity
import com.ext.kernelmanager.domain.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val deviceIdentity: DeviceIdentity? = null,
    val cpuFreq: String = "Updating...",
    val temperature: String = "N/A",
    val ramText: String = "Analyzing memory map...",
    val ramUsagePercent: Float = 0f,
    val isRooted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val identity = systemRepository.getDeviceIdentity()
            _state.value = _state.value.copy(deviceIdentity = identity)
        }
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                val cpu = systemRepository.getCpuFrequency(0)
                val temp = systemRepository.getTemperature()
                val ram = systemRepository.getRamUsage()
                
                val total = ram.first
                val avail = ram.second
                val used = total - avail
                val percent = if (total > 0) (used.toFloat() / total.toFloat()) else 0f
                
                // Human-centric memory text
                val ramStatus = if (total > 0) {
                    "Currently using ${used}MB out of ${total}MB total RAM. ${avail}MB remains available for the system."
                } else {
                    "Waiting for memory statistics..."
                }

                _state.value = _state.value.copy(
                    cpuFreq = cpu,
                    temperature = temp,
                    ramText = ramStatus,
                    ramUsagePercent = percent,
                    isLoading = false
                )
                
                delay(2000)
            }
        }
    }
}
