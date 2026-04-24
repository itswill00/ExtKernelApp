package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.core.sysfs.*
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardcoreTuningRepositoryImpl @Inject constructor(
    private val pathResolver: SysfsPathResolver
) : HardcoreTuningRepository {

    override suspend fun getCpuClusters(): List<CpuCluster> = withContext(Dispatchers.IO) {
        val paths = pathResolver.getCpuClusterPaths()
        paths.mapIndexed { index, path ->
            val id = path.substringAfter("policy").toIntOrNull() ?: index
            val freqs = RootShellManager.execute("cat $path/scaling_available_frequencies").let { 
                if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { f -> f.toLongOrNull() } else emptyList() 
            }
            val govs = RootShellManager.execute("cat $path/scaling_available_governors").let { 
                if (it is RootResult.Success) it.output.trim().split(" ") else emptyList() 
            }
            val cores = RootShellManager.execute("cat $path/affected_cpus").let { 
                if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { c -> c.toIntOrNull() } else listOf(id) 
            }
            CpuCluster(id, path, cores, freqs, govs)
        }
    }

    override suspend fun setClusterGovernor(clusterId: Int, governor: String): Boolean {
        return RootShellManager.execute("echo $governor > /sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_governor") is RootResult.Success
    }

    override suspend fun setClusterMinFreq(clusterId: Int, freq: Long): Boolean {
        return RootShellManager.execute("echo $freq > /sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_min_freq") is RootResult.Success
    }

    override suspend fun setClusterMaxFreq(clusterId: Int, freq: Long): Boolean {
        return RootShellManager.execute("echo $freq > /sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_max_freq") is RootResult.Success
    }

    override suspend fun getClusterCurrentFreq(clusterId: Int): Long {
        val result = RootShellManager.execute("cat /sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_cur_freq")
        return if (result is RootResult.Success) result.output.trim().toLongOrNull() ?: 0L else 0L
    }

    override suspend fun getClusterCurrentGovernor(clusterId: Int): String {
        val result = RootShellManager.execute("cat /sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_governor")
        return if (result is RootResult.Success) result.output.trim() else "Unknown"
    }

    override suspend fun setCoreOnline(coreId: Int, online: Boolean): Boolean {
        val valStr = if (online) "1" else "0"
        return RootShellManager.execute("echo $valStr > /sys/devices/system/cpu/cpu$coreId/online") is RootResult.Success
    }

    override suspend fun isCoreOnline(coreId: Int): Boolean {
        val result = RootShellManager.execute("cat /sys/devices/system/cpu/cpu$coreId/online")
        return if (result is RootResult.Success) result.output.trim() == "1" else true // Default online
    }

    override suspend fun getGpuInfo(): GpuInfo? = withContext(Dispatchers.IO) {
        val path = pathResolver.getGpuPath() ?: return@withContext null
        val type = if (path.contains("kgsl")) GpuType.ADRENO else GpuType.MALI
        val freqPath = if (type == GpuType.ADRENO) "$path/gpu_available_frequencies" else "$path/available_frequencies"
        val freqs = RootShellManager.execute("cat $freqPath").let { 
            if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { f -> f.toLongOrNull() } else emptyList() 
        }
        val govPath = if (type == GpuType.ADRENO) "$path/devfreq/available_governors" else "$path/governor_list"
        val govs = RootShellManager.execute("cat $govPath").let { 
            if (it is RootResult.Success) it.output.trim().split(" ") else emptyList() 
        }
        GpuInfo(path, type, freqs, govs)
    }

    override suspend fun setGpuGovernor(governor: String): Boolean {
        val path = pathResolver.getGpuPath() ?: return false
        val p = if (path.contains("kgsl")) "$path/devfreq/governor" else "$path/governor"
        return RootShellManager.execute("echo $governor > $p") is RootResult.Success
    }

    override suspend fun setGpuMaxFreq(freq: Long): Boolean {
        val path = pathResolver.getGpuPath() ?: return false
        val p = if (path.contains("kgsl")) "$path/max_gpuclk" else "$path/max_freq"
        return RootShellManager.execute("echo $freq > $p") is RootResult.Success
    }

    override suspend fun getGpuCurrentFreq(): Long {
        val path = pathResolver.getGpuPath() ?: return 0L
        val p = if (path.contains("kgsl")) "$path/gpuclk" else "$path/cur_freq"
        val result = RootShellManager.execute("cat $p")
        return if (result is RootResult.Success) result.output.trim().toLongOrNull() ?: 0L else 0L
    }

    override suspend fun getGpuCurrentGovernor(): String {
        val path = pathResolver.getGpuPath() ?: return "N/A"
        val p = if (path.contains("kgsl")) "$path/devfreq/governor" else "$path/governor"
        val result = RootShellManager.execute("cat $p")
        return if (result is RootResult.Success) result.output.trim() else "N/A"
    }

    override suspend fun getBlockDevices(): List<String> = pathResolver.getBlockDevices()

    override suspend fun getAvailableIoSchedulers(device: String): List<String> {
        val result = RootShellManager.execute("cat $device/queue/scheduler")
        return if (result is RootResult.Success) result.output.trim().replace("[", "").replace("]", "").split(" ") else emptyList()
    }

    override suspend fun getCurrentIoScheduler(device: String): String {
        val result = RootShellManager.execute("cat $device/queue/scheduler")
        return if (result is RootResult.Success) result.output.substringAfter("[").substringBefore("]") else "Unknown"
    }

    override suspend fun setIoScheduler(device: String, scheduler: String): Boolean {
        return RootShellManager.execute("echo $scheduler > $device/queue/scheduler") is RootResult.Success
    }

    override suspend fun setReadAhead(device: String, kb: Int): Boolean {
        return RootShellManager.execute("echo $kb > $device/queue/read_ahead_kb") is RootResult.Success
    }

    override suspend fun getReadAhead(device: String): Int {
        val result = RootShellManager.execute("cat $device/queue/read_ahead_kb")
        return if (result is RootResult.Success) result.output.trim().toIntOrNull() ?: 128 else 128
    }

    override suspend fun readSysfs(path: String): String {
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim() else "Error"
    }

    override suspend fun writeSysfs(path: String, value: String): Boolean {
        return RootShellManager.execute("echo $value > $path") is RootResult.Success
    }
}
