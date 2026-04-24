package com.ext.kernelmanager.presentation.screens.memory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryState(
    val isZramSupported: Boolean = false,
    val zramSizeMb: Int = 0,
    val swappiness: Int = 60,
    val lmkProfile: String = "Balanced",
    val lmkProfiles: List<String> = listOf("Light", "Balanced", "Aggressive"),
    val isLoading: Boolean = true,
    val infoMessage: String? = null
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state.asStateFlow()

    init {
        loadMemoryData()
    }

    fun loadMemoryData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val zramSupported = memoryRepository.isZramSupported()
            if (!zramSupported) {
                _state.value = _state.value.copy(
                    isZramSupported = false,
                    infoMessage = "This device uses factory default memory management that cannot be modified.",
                    isLoading = false
                )
                return@launch
            }

            val size = memoryRepository.getZramSizeMb()
            val swapp = memoryRepository.getSwappiness()
            val lmk = memoryRepository.getCurrentLmkProfile()

            _state.value = _state.value.copy(
                isZramSupported = true,
                zramSizeMb = size,
                swappiness = swapp,
                lmkProfile = lmk,
                isLoading = false,
                infoMessage = null
            )
        }
    }

    fun applyZramSize(sizeMb: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = memoryRepository.setZramSizeMb(sizeMb)
            if (success) {
                _state.value = _state.value.copy(zramSizeMb = sizeMb, infoMessage = "Virtual memory size changed successfully.")
            } else {
                _state.value = _state.value.copy(infoMessage = "Failed to change virtual memory size.")
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun applySwappiness(value: Int) {
        viewModelScope.launch {
            memoryRepository.setSwappiness(value)
            _state.value = _state.value.copy(swappiness = value)
        }
    }

    fun applyLmkProfile(profile: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = memoryRepository.setLmkProfile(profile)
            if (success) {
                _state.value = _state.value.copy(lmkProfile = profile, infoMessage = "RAM profile $profile applied successfully.")
            } else {
                _state.value = _state.value.copy(infoMessage = "Failed to apply RAM profile.")
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(infoMessage = null)
    }
}
