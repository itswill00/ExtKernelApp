package com.ext.kernelmanager.presentation.screens.technical.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ext.kernelmanager.core.sysfs.CpuCluster
import com.ext.kernelmanager.core.sysfs.GpuInfo
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TechnicalState(
    val clusters: List<CpuClusterState> = emptyList(),
    val gpu: GpuState? = null,
    val isLoading: Boolean = true
)

data class CpuClusterState(
    val id: Int,
    val currentFreq: Long,
    val maxFreq: Long,
    val minFreq: Long,
    val governor: String,
    val availableFrequencies: List<Long>,
    val availableGovernors: List<String>
)

data class GpuState(
    val currentFreq: Long,
    val maxFreq: Long,
    val governor: String,
    val availableFrequencies: List<Long>,
    val availableGovernors: List<String>
)

@HiltViewModel
class TechnicalDashboardViewModel @Inject constructor(
    private val tuningRepository: HardcoreTuningRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TechnicalState())
    val state: StateFlow<TechnicalState> = _state.asStateFlow()

    init {
        startRealTimeUpdates()
    }

    private fun startRealTimeUpdates() {
        viewModelScope.launch {
            val clusters = tuningRepository.getCpuClusters()
            val gpuInfo = tuningRepository.getGpuInfo()

            while (true) {
                val clusterStates = clusters.map { cluster ->
                    val current = tuningRepository.getClusterCurrentFreq(cluster.id)
                    val gov = tuningRepository.getClusterCurrentGovernor(cluster.id)
                    CpuClusterState(
                        id = cluster.id,
                        currentFreq = current,
                        maxFreq = cluster.availableFrequencies.maxOrNull() ?: 0,
                        minFreq = cluster.availableFrequencies.minOrNull() ?: 0,
                        governor = gov,
                        availableFrequencies = cluster.availableFrequencies,
                        availableGovernors = cluster.availableGovernors
                    )
                }

                val gpuState = gpuInfo?.let {
                    GpuState(
                        currentFreq = tuningRepository.getGpuCurrentFreq(),
                        maxFreq = it.availableFrequencies.maxOrNull() ?: 0,
                        governor = tuningRepository.getGpuCurrentGovernor(),
                        availableFrequencies = it.availableFrequencies,
                        availableGovernors = it.availableGovernors
                    )
                }

                _state.value = TechnicalState(
                    clusters = clusterStates,
                    gpu = gpuState,
                    isLoading = false
                )
                
                delay(1000)
            }
        }
    }

    fun updateClusterGovernor(clusterId: Int, gov: String) {
        viewModelScope.launch { tuningRepository.setClusterGovernor(clusterId, gov) }
    }

    fun updateGpuGovernor(gov: String) {
        viewModelScope.launch { tuningRepository.setGpuGovernor(gov) }
    }
}
