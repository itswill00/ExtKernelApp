package com.ext.kernelmanager.core.hardware

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import java.io.File

/**
 * Mendeteksi spesifikasi sistem dengan cara yang dinamis.
 */
class HardwareDetector {

    /**
     * Mendapatkan nama model perangkat dan versi kernel.
     */
    fun getDeviceInfo(): DeviceIdentity {
        val kernelVersion = System.getProperty("os.version") ?: "Versi kernel tidak terdeteksi"
        val model = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        
        return DeviceIdentity(
            model = model,
            kernel = kernelVersion,
            androidVersion = android.os.Build.VERSION.RELEASE
        )
    }

    /**
     * Membaca informasi CPU (Max Frequency) secara aman.
     * Menggunakan strategi pencarian path dinamis (Auto-Detect).
     */
    /**
     * Membaca suhu perangkat secara dinamis dari berbagai thermal zone.
     */
    suspend fun getDeviceTemp(): String {
        // Daftar path thermal yang umum pada Android
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone7/temp" // Seringkali CPU/Battery pada beberapa chipset
        )

        for (path in thermalPaths) {
            if (File(path).exists()) {
                val result = RootShellManager.execute("cat $path")
                if (result is RootResult.Success) {
                    val rawTemp = result.output.trim().toFloatOrNull() ?: continue
                    // Konversi dari miliderajat (misal 45000) ke derajat (45.0)
                    val temp = if (rawTemp > 1000) rawTemp / 1000 else rawTemp
                    return "${temp.toInt()}°C"
                }
            }
        }
        return "N/A"
    }

    /**
     * Membaca sisa RAM dari /proc/meminfo.
     */
    suspend fun getRamStatus(): Pair<Long, Long> {
        val result = RootShellManager.execute("cat /proc/meminfo")
        if (result is RootResult.Success) {
            val lines = result.output.split("\n")
            val total = lines.find { it.startsWith("MemTotal:") }?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
            val available = lines.find { it.startsWith("MemAvailable:") }?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
            return Pair(total / 1024, available / 1024) // Mengembalikan dalam MB
        }
        return Pair(0L, 0L)
    }

    suspend fun getCpuMaxFreq(coreIndex: Int = 0): String {
        val path = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_max_freq"
        
        return if (File(path).exists()) {
            val result = RootShellManager.execute("cat $path")
            when (result) {
                is RootResult.Success -> "${result.output.trim().toInt() / 1000} MHz"
                else -> "N/A"
            }
        } else {
            // Fallback: Beberapa perangkat menggunakan path berbeda atau terkunci
            "Tidak didukung"
        }
    }
}

data class DeviceIdentity(
    val model: String,
    val kernel: String,
    val androidVersion: String
)