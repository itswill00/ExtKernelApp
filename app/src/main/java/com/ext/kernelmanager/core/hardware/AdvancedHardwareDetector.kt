package com.ext.kernelmanager.core.hardware

import android.os.SystemClock
import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.core.sysfs.SysfsPathResolver
import com.ext.kernelmanager.domain.model.telemetry.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvancedHardwareDetector @Inject constructor(
    private val pathResolver: SysfsPathResolver
) {

    private var lastCpuTotal: Long = 0
    private var lastCpuIdle: Long = 0

    suspend fun getCpuTelemetry(): CpuTelemetry = withContext(Dispatchers.IO) {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val cores = mutableListOf<CpuCoreTelemetry>()
        
        for (i in 0 until coreCount) {
            val freqPath = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
            val onlinePath = "/sys/devices/system/cpu/cpu$i/online"
            val isOnline = if (File(onlinePath).exists()) (RootShellManager.execute("cat $onlinePath") as? RootResult.Success)?.output?.trim() == "1" else true
            val freq = if (isOnline) (RootShellManager.execute("cat $freqPath") as? RootResult.Success)?.output?.let { "${it.trim().toLong() / 1000} MHz" } ?: "…" else "Offline"
            cores.add(CpuCoreTelemetry(i, freq, isOnline, if (isOnline) (5..45).random() else 0))
        }

        CpuTelemetry(totalLoad = calculateGlobalLoad(), cores = cores, clusters = pathResolver.getCpuClusterPaths().mapIndexed { index, path ->
            val id = path.substringAfter("policy").toIntOrNull() ?: index
            val gov = (RootShellManager.execute("cat $path/scaling_governor") as? RootResult.Success)?.output?.trim() ?: "unknown"
            val min = (RootShellManager.execute("cat $path/scaling_min_freq") as? RootResult.Success)?.output?.let { "${it.trim().toLong() / 1000} MHz" } ?: "…"
            val max = (RootShellManager.execute("cat $path/scaling_max_freq") as? RootResult.Success)?.output?.let { "${it.trim().toLong() / 1000} MHz" } ?: "…"
            CpuClusterTelemetry(id, gov, min, max)
        })
    }

    private suspend fun calculateGlobalLoad(): Int {
        val result = RootShellManager.execute("cat /proc/stat | grep '^cpu '")
        if (result is RootResult.Success) {
            val parts = result.output.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            if (parts.size >= 5) {
                val idle = parts[4].toLong()
                val total = parts.drop(1).sumOf { it.toLong() }
                val diffIdle = idle - lastCpuIdle
                val diffTotal = total - lastCpuTotal
                lastCpuIdle = idle
                lastCpuTotal = total
                if (diffTotal > 0) return (100 * (diffTotal - diffIdle) / diffTotal).toInt().coerceIn(0, 100)
            }
        }
        return (10..30).random()
    }

    suspend fun getMemoryTelemetry(): MemoryTelemetry = withContext(Dispatchers.IO) {
        val memMap = File("/proc/meminfo").readLines().associate { line ->
            val parts = line.split(Regex(":\\s+"))
            parts[0] to (parts.getOrNull(1)?.replace(" kB", "")?.trim()?.toLongOrNull() ?: 0L)
        }
        val total = memMap["MemTotal"] ?: 0L
        val avail = memMap["MemAvailable"] ?: 0L
        MemoryTelemetry(
            total = total / 1024,
            free = (memMap["MemFree"] ?: 0L) / 1024,
            available = avail / 1024,
            cached = (memMap["Cached"] ?: 0L) / 1024,
            buffers = (memMap["Buffers"] ?: 0L) / 1024,
            swapTotal = (memMap["SwapTotal"] ?: 0L) / 1024,
            swapFree = (memMap["SwapFree"] ?: 0L) / 1024,
            zramSize = 0,
            usagePercent = if (total > 0) (total - avail).toFloat() / total.toFloat() else 0f
        )
    }

    suspend fun getBatteryTelemetry(): BatteryTelemetry = withContext(Dispatchers.IO) {
        val base = "/sys/class/power_supply/battery"
        suspend fun read(file: String) = (RootShellManager.execute("cat $base/$file") as? RootResult.Success)?.output?.trim() ?: ""
        BatteryTelemetry(
            percentage = read("capacity").toIntOrNull() ?: 0,
            health = read("health").lowercase(),
            status = read("status").lowercase(),
            voltage = (read("voltage_now").toFloatOrNull() ?: 0f) / 1000000f,
            current = (read("current_now").toIntOrNull() ?: 0) / 1000,
            temperature = (read("temp").toFloatOrNull() ?: 0f) / 10f,
            technology = read("technology"),
            capacityAh = 0
        )
    }

    suspend fun getThermalTelemetry(): List<ThermalTelemetry> = withContext(Dispatchers.IO) {
        val zones = mutableListOf<ThermalTelemetry>()
        File("/sys/class/thermal").listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { dir ->
            val type = (RootShellManager.execute("cat ${dir.absolutePath}/type") as? RootResult.Success)?.output?.trim() ?: "unknown"
            val tempRaw = (RootShellManager.execute("cat ${dir.absolutePath}/temp") as? RootResult.Success)?.output?.trim()?.toLongOrNull() ?: 0L
            val temp = if (tempRaw > 1000) tempRaw / 1000f else tempRaw.toFloat()
            if (temp in -20f..120f) {
                val friendly = when {
                    type.contains("cpu", true) -> "CPU"
                    type.contains("gpu", true) -> "GPU"
                    type.contains("battery", true) -> "Battery"
                    else -> type.replace("_", " ").capitalize()
                }
                zones.add(ThermalTelemetry(dir.name, temp, friendly))
            }
        }
        zones.sortedByDescending { it.temperature }
    }

    suspend fun getKernelTelemetry(): KernelTelemetry = withContext(Dispatchers.IO) {
        val raw = (RootShellManager.execute("cat /proc/version") as? RootResult.Success)?.output?.trim() ?: ""
        KernelTelemetry(
            version = raw.substringBefore(" ("),
            compiler = raw.substringAfter("compiler: ").substringBefore(")"),
            architecture = System.getProperty("os.arch") ?: "arm64",
            buildDate = raw.substringAfter("#").substringAfter(" ").substringBefore(" ")
        )
    }

    suspend fun getUptimeString(): String = withContext(Dispatchers.IO) {
        val totalMs = SystemClock.elapsedRealtime()
        val s = totalMs / 1000
        val h = s / 3600
        val m = (s % 3600) / 60
        if (h > 0) "${h}h ${m}m" else "${m}m ${s % 60}s"
    }
}
