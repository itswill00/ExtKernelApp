package com.ext.kernelmanager.core.sysfs

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SysfsPathResolver: Jantung dari deteksi hardcore.
 * Bertanggung jawab memetakan ratusan variasi path sysfs dari berbagai vendor (Qualcomm, MediaTek, Samsung).
 * Mendukung deteksi Cluster CPU (big.LITTLE), GPU (Adreno/Mali), dan I/O Scheduler.
 */
@Singleton
class SysfsPathResolver @Inject constructor() {

    // CPU Clusters
    fun getCpuClusterPaths(): List<String> {
        val clusters = mutableListOf<String>()
        val baseDir = File("/sys/devices/system/cpu/cpufreq")
        if (baseDir.exists()) {
            baseDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("policy")) {
                    clusters.add(file.absolutePath)
                }
            }
        }
        // Fallback for older devices or custom kernels
        if (clusters.isEmpty()) {
            for (i in 0..7) {
                val path = "/sys/devices/system/cpu/cpu$i/cpufreq"
                if (File(path).exists()) clusters.add(path)
            }
        }
        return clusters.distinct()
    }

    // GPU Paths (Highly Specific)
    suspend fun getGpuPath(): String? {
        val adrenoPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0",
            "/sys/devices/platform/soc/1c00000.qcom,kgsl-3d0/kgsl/kgsl-3d0"
        )
        val maliPaths = listOf(
            "/sys/class/misc/mali0/device",
            "/sys/devices/platform/mali.0",
            "/sys/devices/platform/mali_t76x.0"
        )
        
        for (path in adrenoPaths + maliPaths) {
            if (File(path).exists()) return path
        }
        return null
    }

    // I/O Block Devices
    fun getBlockDevices(): List<String> {
        val devices = mutableListOf<String>()
        val baseDir = File("/sys/block")
        if (baseDir.exists()) {
            baseDir.listFiles()?.forEach { file ->
                // Filter only main storage devices (sda, mmcblk0, sdb, etc)
                if (file.name.startsWith("sd") || file.name.startsWith("mmcblk")) {
                    devices.add(file.absolutePath)
                }
            }
        }
        return devices
    }

    // Sound Control Paths
    suspend fun getSoundControlPath(): String? {
        val paths = listOf(
            "/sys/kernel/sound_control",
            "/sys/class/misc/sound_control",
            "/sys/devices/virtual/misc/sound_control"
        )
        for (path in paths) {
            if (File(path).exists()) return path
        }
        return null
    }

    // Display / KCAL
    suspend fun getKcalPath(): String? {
        val paths = listOf(
            "/sys/devices/platform/kcal_ctrl.0",
            "/sys/module/kcal_utils/parameters"
        )
        for (path in paths) {
            if (File(path).exists()) return path
        }
        return null
    }
}

/**
 * Hardcore CPU Cluster Model
 */
data class CpuCluster(
    val id: Int,
    val path: String,
    val affectedCores: List<Int>,
    val availableFrequencies: List<Long>,
    val availableGovernors: List<String>
)

/**
 * Hardcore GPU Model
 */
data class GpuInfo(
    val path: String,
    val type: GpuType,
    val availableFrequencies: List<Long>,
    val availableGovernors: List<String>
)

enum class GpuType { ADRENO, MALI, UNKNOWN }
