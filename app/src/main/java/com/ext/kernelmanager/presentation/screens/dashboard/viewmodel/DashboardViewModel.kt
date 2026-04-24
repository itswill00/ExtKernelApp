package com.ext.kernelmanager.presentation.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.model.telemetry.SystemTelemetry
import com.ext.kernelmanager.domain.usecase.hardware.GetSystemTelemetryUseCase
import com.ext.kernelmanager.domain.repository.SystemRepository
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
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTelemetryUseCase: GetSystemTelemetryUseCase,
    private val systemRepository: SystemRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        checkRootAndStart()
    }

    private fun checkRootAndStart() {
        viewModelScope.launch {
            val root = systemRepository.isRootAvailable()
            _state.value = _state.value.copy(isRooted = root)
            if (root) {
                startTelemetryLoop()
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Root access denied.")
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                try {
                    val data = getTelemetryUseCase.execute()
                    _state.value = _state.value.copy(
                        telemetry = data,
                        isLoading = false,
                        error = null
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(error = e.message)
                }
                delay(2000)
            }
        }
    }
}
