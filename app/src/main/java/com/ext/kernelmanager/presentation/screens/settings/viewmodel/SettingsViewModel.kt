package com.ext.kernelmanager.presentation.screens.settings.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.data.local.SettingsBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val infoMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: SettingsBackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun exportSettings(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = backupManager.exportSettings(uri)
            if (success) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    infoMessage = "Settings exported successfully. You can restore them anytime."
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    infoMessage = "Failed to export settings. Ensure storage is not full."
                )
            }
        }
    }

    fun importSettings(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = backupManager.importSettings(uri)
            if (success) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    infoMessage = "Settings restored successfully. Changes will take effect immediately."
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    infoMessage = "Failed to restore settings. The file might be corrupted or incompatible."
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(infoMessage = null)
    }
}
