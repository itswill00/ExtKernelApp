package com.ext.kernelmanager.domain.model.hardware

import com.ext.kernelmanager.core.node.TunableNode

/**
 * CpuCluster: A high-level abstraction of a CPU frequency cluster.
 * Dynamically populated by the discovery engine.
 */
class CpuCluster(
    val id: Int,
    val basePath: String
) {
    val curFreq = TunableNode("$basePath/scaling_cur_freq")
    val maxFreq = TunableNode("$basePath/scaling_max_freq")
    val minFreq = TunableNode("$basePath/scaling_min_freq")
    val governor = TunableNode("$basePath/scaling_governor")
    val availableFreqs = TunableNode("$basePath/scaling_available_frequencies")
    val availableGovernors = TunableNode("$basePath/scaling_available_governors")
    val affectedCpus = TunableNode("$basePath/affected_cpus")

    suspend fun getFrequencies(): List<Long> {
        return availableFreqs.read().split(" ")
            .mapNotNull { it.toLongOrNull() }
            .sorted()
    }

    suspend fun getGovernors(): List<String> {
        return availableGovernors.read().split(" ")
            .filter { it.isNotBlank() }
    }

    suspend fun getCoreIds(): List<Int> {
        return affectedCpus.read().split(" ")
            .mapNotNull { it.toIntOrNull() }
    }
}

/**
 * GpuController: Abstracts over Adreno/Mali/PowerVR differences.
 */
class GpuController(val basePath: String, val vendor: String) {
    
    // Dynamically resolve paths based on vendor signature
    private val freqPath = if (vendor == "qcom") "gpuclk" else "cur_freq"
    private val maxFreqPath = if (vendor == "qcom") "max_gpuclk" else "max_freq"
    private val govPath = if (vendor == "qcom") "devfreq/governor" else "governor"

    val curFreq = TunableNode("$basePath/$freqPath")
    val maxFreq = TunableNode("$basePath/$maxFreqPath")
    val governor = TunableNode("$basePath/$govPath")
    
    suspend fun isSupported(): Boolean = curFreq.exists()
}
