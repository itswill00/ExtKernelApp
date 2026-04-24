package com.ext.kernelmanager.presentation.screens.battery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatteryState(
    val currentProfile: String = "Balanced",
    val isLoading: Boolean = false,
    val infoMessage: String? = null
)

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BatteryState(isLoading = true))
    val state: StateFlow<BatteryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val current = batteryRepository.getCurrentProfile()
            _state.value = BatteryState(currentProfile = current, isLoading = false)
        }
    }

    fun applyProfile(profileName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, infoMessage = "Applying $profileName profile. Please wait...")
            
            val success = batteryRepository.applyProfile(profileName)
            
            if (success) {
                _state.value = _state.value.copy(
                    currentProfile = profileName,
                    isLoading = false,
                    infoMessage = "$profileName profile applied successfully. System is now running with this configuration."
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    infoMessage = "Failed to apply $profileName profile. Root access might not be granted."
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(infoMessage = null)
    }
}
