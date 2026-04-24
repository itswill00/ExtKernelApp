package com.ext.kernelmanager.core.sysfs

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SysfsPathResolver @Inject constructor() {

    // CPU Frequencies & Clusters
    val CPU_BASE_PATH = "/sys/devices/system/cpu"
    val CPU_POLICY_BASE = "$CPU_BASE_PATH/cpufreq"
    
    // GPU
    val GPU_ADRENO_PATH = "/sys/class/kgsl/kgsl-3d0"
    val GPU_MALI_PATH = "/sys/devices/platform/mali.0"

    // Thermal
    val THERMAL_PATH = "/sys/class/thermal"

    // CPU Clusters discovery
    fun getCpuClusterPaths(): List<String> {
        val clusters = mutableListOf<String>()
        val baseDir = File(CPU_POLICY_BASE)
        if (baseDir.exists()) {
            baseDir.listFiles()?.filter { it.name.startsWith("policy") }?.forEach { 
                clusters.add(it.absolutePath) 
            }
        }
        if (clusters.isEmpty()) {
            for (i in 0..7) {
                val path = "$CPU_BASE_PATH/cpu$i/cpufreq"
                if (File(path).exists()) clusters.add(path)
            }
        }
        return clusters.sorted()
    }

    // Advanced: CPU Hotplug detection
    fun getHotplugPaths(): List<String> {
        return listOf(
            "/sys/module/msm_hotplug",
            "/sys/module/intelli_plug",
            "/sys/module/blu_plug",
            "/sys/module/auto_smp",
            "/sys/devices/system/cpu/cpuhotplug"
        ).filter { File(it).exists() }
    }

    // Advanced: Thermal mitigation paths
    fun getThermalControlPaths(): List<String> {
        return listOf(
            "/sys/module/msm_thermal",
            "/sys/devices/virtual/thermal/thermal_message"
        ).filter { File(it).exists() }
    }

    // GPU path discovery
    fun getGpuPath(): String? {
        val paths = listOf(GPU_ADRENO_PATH, GPU_MALI_PATH, "/sys/devices/platform/soc/1c00000.qcom,kgsl-3d0/kgsl/kgsl-3d0")
        return paths.find { File(it).exists() }
    }

    // Screen / KCAL
    fun getKcalPath(): String? {
        val paths = listOf(
            "/sys/devices/platform/kcal_ctrl.0",
            "/sys/module/kcal_utils/parameters"
        )
        return paths.find { File(it).exists() }
    }

    // Sound
    fun getSoundControlPath(): String? {
        val paths = listOf(
            "/sys/kernel/sound_control",
            "/sys/class/misc/sound_control"
        )
        return paths.find { File(it).exists() }
    }

    // I/O Block Devices
    fun getBlockDevices(): List<String> {
        val devices = mutableListOf<String>()
        val baseDir = File("/sys/block")
        if (baseDir.exists()) {
            baseDir.listFiles()?.filter { 
                it.name.startsWith("sd") || it.name.startsWith("mmcblk") || it.name.startsWith("dm-")
            }?.forEach { devices.add(it.absolutePath) }
        }
        return devices
    }
}
