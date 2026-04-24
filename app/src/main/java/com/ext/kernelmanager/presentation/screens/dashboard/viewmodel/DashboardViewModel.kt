package com.ext.kernelmanager.presentation.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.core.hardware.DeviceIdentity
import com.ext.kernelmanager.domain.repository.SystemRepository
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
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
    val batteryCapacity: Int = 0,
    val batteryHealth: String = "N/A",
    val uptime: String = "N/A",
    val gpuFreq: String = "N/A",
    val isRooted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    private val tuningRepository: HardcoreTuningRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val identity = systemRepository.getDeviceIdentity()
            val rootStatus = systemRepository.isRootAvailable()
            _state.value = _state.value.copy(
                deviceIdentity = identity,
                isRooted = rootStatus
            )
        }
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                val cpu = systemRepository.getCpuFrequency(0)
                val temp = systemRepository.getTemperature()
                val ram = systemRepository.getRamUsage()
                val battery = systemRepository.getBatteryInfo()
                val uptimeStr = systemRepository.getUptime()
                val gpu = tuningRepository.getGpuCurrentFreq()
                
                val total = ram.first
                val avail = ram.second
                val used = total - avail
                val percent = if (total > 0) (used.toFloat() / total.toFloat()) else 0f
                
                val ramStatus = "Used: ${used}MB / Total: ${total}MB"

                _state.value = _state.value.copy(
                    cpuFreq = cpu,
                    temperature = temp,
                    ramText = ramStatus,
                    ramUsagePercent = percent,
                    batteryCapacity = battery.first,
                    batteryHealth = battery.second,
                    uptime = uptimeStr,
                    gpuFreq = if (gpu > 0) "${gpu / 1000000} MHz" else "N/A",
                    isLoading = false
                )
                
                delay(2000)
            }
        }
    }
}
