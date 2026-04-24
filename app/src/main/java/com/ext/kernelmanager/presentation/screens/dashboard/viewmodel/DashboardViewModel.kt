package com.ext.kernelmanager.presentation.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.core.hardware.DeviceIdentity
import com.ext.kernelmanager.domain.model.telemetry.SystemTelemetry
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
    val telemetry: SystemTelemetry? = null,
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
            val rootStatus = systemRepository.isRootAvailable()
            _state.value = _state.value.copy(isRooted = rootStatus)
        }
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                try {
                    val fullTelemetry = systemRepository.getFullTelemetry()
                    
                    // Fetch real GPU data using HardcoreTuningRepository
                    val gpuInfo = tuningRepository.getGpuInfo()
                    val gpuFreq = tuningRepository.getGpuCurrentFreq()
                    val gpuGov = tuningRepository.getGpuCurrentGovernor()
                    
                    val enrichedTelemetry = fullTelemetry.copy(
                        gpu = fullTelemetry.gpu.copy(
                            currentFreq = if (gpuFreq > 0) "${gpuFreq / 1000000} MHz" else "N/A",
                            governor = gpuGov
                        )
                    )

                    _state.value = _state.value.copy(
                        telemetry = enrichedTelemetry,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    // Log error but keep loop running
                }
                delay(2000)
            }
        }
    }
}
