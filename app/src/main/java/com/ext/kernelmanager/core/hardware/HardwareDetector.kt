package com.ext.kernelmanager.core.hardware

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import java.io.File

/**
 * Detects system specifications dynamically.
 */
class HardwareDetector {

    /**
     * Gets device model name and kernel version.
     */
    fun getDeviceInfo(): DeviceIdentity {
        val kernelVersion = System.getProperty("os.version") ?: "Kernel version not detected"
        val model = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        
        return DeviceIdentity(
            model = model,
            kernel = kernelVersion,
            androidVersion = android.os.Build.VERSION.RELEASE
        )
    }

    /**
     * Reads device temperature dynamically from various thermal zones.
     */
    suspend fun getDeviceTemp(): String {
        // List of common Android thermal paths
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone7/temp"
        )

        for (path in thermalPaths) {
            if (File(path).exists()) {
                val result = RootShellManager.execute("cat $path")
                if (result is RootResult.Success) {
                    val rawTemp = result.output.trim().toFloatOrNull() ?: continue
                    // Convert from millidegrees (e.g., 45000) to degrees (45.0)
                    val temp = if (rawTemp > 1000) rawTemp / 1000 else rawTemp
                    return "${temp.toInt()}°C"
                }
            }
        }
        return "N/A"
    }

    /**
     * Reads available RAM from /proc/meminfo.
     */
    suspend fun getRamStatus(): Pair<Long, Long> {
        val result = RootShellManager.execute("cat /proc/meminfo")
        if (result is RootResult.Success) {
            val lines = result.output.split("\n")
            val total = lines.find { it.startsWith("MemTotal:") }?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
            val available = lines.find { it.startsWith("MemAvailable:") }?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
            return Pair(total / 1024, available / 1024) // Returns in MB
        }
        return Pair(0L, 0L)
    }

    /**
     * Reads CPU information (Max Frequency) securely.
     * Uses dynamic path discovery strategy (Auto-Detect).
     */
    suspend fun getCpuMaxFreq(coreIndex: Int = 0): String {
        val path = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_max_freq"
        
        return if (File(path).exists()) {
            val result = RootShellManager.execute("cat $path")
            when (result) {
                is RootResult.Success -> "${result.output.trim().toInt() / 1000} MHz"
                else -> "N/A"
            }
        } else {
            // Fallback: Some devices use different or locked paths
            "Unsupported"
        }
    }
}

data class DeviceIdentity(
    val model: String,
    val kernel: String,
    val androidVersion: String
)
