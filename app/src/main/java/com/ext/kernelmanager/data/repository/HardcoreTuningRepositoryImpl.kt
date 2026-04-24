package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.core.sysfs.*
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
            val freqs = RootShellManager.execute("cat $path/scaling_available_frequencies")
                .let { if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { f -> f.toLongOrNull() } else emptyList() }
            val govs = RootShellManager.execute("cat $path/scaling_available_governors")
                .let { if (it is RootResult.Success) it.output.trim().split(" ") else emptyList() }
            val cores = RootShellManager.execute("cat $path/affected_cpus")
                .let { if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { c -> c.toIntOrNull() } else listOf(id) }
            CpuCluster(id, path, cores, freqs, govs)
        }
    }

    override suspend fun setClusterGovernor(clusterId: Int, governor: String): Boolean {
        val path = "/sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_governor"
        return RootShellManager.execute("echo $governor > $path") is RootResult.Success
    }

    override suspend fun setClusterMinFreq(clusterId: Int, freq: Long): Boolean {
        val path = "/sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_min_freq"
        return RootShellManager.execute("echo $freq > $path") is RootResult.Success
    }

    override suspend fun setClusterMaxFreq(clusterId: Int, freq: Long): Boolean {
        val path = "/sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_max_freq"
        return RootShellManager.execute("echo $freq > $path") is RootResult.Success
    }

    override suspend fun getClusterCurrentFreq(clusterId: Int): Long {
        val path = "/sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_cur_freq"
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim().toLongOrNull() ?: 0L else 0L
    }

    override suspend fun getClusterCurrentGovernor(clusterId: Int): String {
        val path = "/sys/devices/system/cpu/cpufreq/policy$clusterId/scaling_governor"
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim() else "Unknown"
    }

    override suspend fun getGpuInfo(): GpuInfo? = withContext(Dispatchers.IO) {
        val path = pathResolver.getGpuPath() ?: return@withContext null
        val type = if (path.contains("kgsl")) GpuType.ADRENO else GpuType.MALI
        val freqPath = if (type == GpuType.ADRENO) "$path/gpu_available_frequencies" else "$path/available_frequencies"
        val freqs = RootShellManager.execute("cat $freqPath")
            .let { if (it is RootResult.Success) it.output.trim().split(" ").mapNotNull { f -> f.toLongOrNull() } else emptyList() }
        val govPath = if (type == GpuType.ADRENO) "$path/devfreq/available_governors" else "$path/governor_list"
        val govs = RootShellManager.execute("cat $govPath")
            .let { if (it is RootResult.Success) it.output.trim().split(" ") else emptyList() }
        GpuInfo(path, type, freqs, govs)
    }

    override suspend fun setGpuGovernor(governor: String): Boolean {
        val path = pathResolver.getGpuPath() ?: return false
        val govPath = if (path.contains("kgsl")) "$path/devfreq/governor" else "$path/governor"
        return RootShellManager.execute("echo $governor > $govPath") is RootResult.Success
    }

    override suspend fun setGpuMaxFreq(freq: Long): Boolean {
        val path = pathResolver.getGpuPath() ?: return false
        val freqPath = if (path.contains("kgsl")) "$path/max_gpuclk" else "$path/max_freq"
        return RootShellManager.execute("echo $freq > $freqPath") is RootResult.Success
    }

    override suspend fun getGpuCurrentFreq(): Long {
        val path = pathResolver.getGpuPath() ?: return 0L
        val freqPath = if (path.contains("kgsl")) "$path/gpuclk" else "$path/cur_freq"
        val result = RootShellManager.execute("cat $freqPath")
        return if (result is RootResult.Success) result.output.trim().toLongOrNull() ?: 0L else 0L
    }

    override suspend fun getGpuCurrentGovernor(): String {
        val path = pathResolver.getGpuPath() ?: return "N/A"
        val govPath = if (path.contains("kgsl")) "$path/devfreq/governor" else "$path/governor"
        val result = RootShellManager.execute("cat $govPath")
        return if (result is RootResult.Success) result.output.trim() else "N/A"
    }

    override suspend fun getBlockDevices(): List<String> = pathResolver.getBlockDevices()

    override suspend fun getAvailableIoSchedulers(device: String): List<String> {
        val path = "$device/queue/scheduler"
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim().replace("[", "").replace("]", "").split(" ") else emptyList()
    }

    override suspend fun getCurrentIoScheduler(device: String): String {
        val path = "$device/queue/scheduler"
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.substringAfter("[").substringBefore("]") else "Unknown"
    }

    override suspend fun setIoScheduler(device: String, scheduler: String): Boolean {
        val path = "$device/queue/scheduler"
        return RootShellManager.execute("echo $scheduler > $path") is RootResult.Success
    }

    override suspend fun setReadAhead(device: String, kb: Int): Boolean {
        val path = "$device/queue/read_ahead_kb"
        return RootShellManager.execute("echo $kb > $path") is RootResult.Success
    }

    override suspend fun getReadAhead(device: String): Int {
        val path = "$device/queue/read_ahead_kb"
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim().toIntOrNull() ?: 128 else 128
    }

    override suspend fun setIoStats(device: String, enabled: Boolean): Boolean {
        val path = "$device/queue/iostats"
        val valStr = if (enabled) "1" else "0"
        return RootShellManager.execute("echo $valStr > $path") is RootResult.Success
    }

    override suspend fun setAddRandom(device: String, enabled: Boolean): Boolean {
        val path = "$device/queue/add_random"
        val valStr = if (enabled) "1" else "0"
        return RootShellManager.execute("echo $valStr > $path") is RootResult.Success
    }

    override suspend fun isKcalSupported(): Boolean = pathResolver.getKcalPath() != null

    override suspend fun setKcalRgb(r: Int, g: Int, b: Int): Boolean {
        val path = pathResolver.getKcalPath() ?: return false
        return RootShellManager.execute("echo \"$r $g $b\" > $path/kcal") is RootResult.Success
    }

    override suspend fun setKcalSaturation(valSat: Int): Boolean {
        val path = pathResolver.getKcalPath() ?: return false
        return RootShellManager.execute("echo $valSat > $path/kcal_sat") is RootResult.Success
    }

    override suspend fun setKcalContrast(valCont: Int): Boolean {
        val path = pathResolver.getKcalPath() ?: return false
        return RootShellManager.execute("echo $valCont > $path/kcal_cont") is RootResult.Success
    }

    override suspend fun getKcalValues(): Map<String, Int> {
        val path = pathResolver.getKcalPath() ?: return emptyMap()
        val kcal = RootShellManager.execute("cat $path/kcal").let { if (it is RootResult.Success) it.output.trim().split(" ") else listOf("256", "256", "256") }
        return mapOf("r" to (kcal.getOrNull(0)?.toIntOrNull() ?: 256), "g" to (kcal.getOrNull(1)?.toIntOrNull() ?: 256), "b" to (kcal.getOrNull(2)?.toIntOrNull() ?: 256))
    }

    override suspend fun isSoundControlSupported(): Boolean = pathResolver.getSoundControlPath() != null

    override suspend fun setSpeakerGain(gain: Int): Boolean {
        val path = pathResolver.getSoundControlPath() ?: return false
        return RootShellManager.execute("echo $gain > $path/speaker_gain") is RootResult.Success
    }

    override suspend fun setHeadphoneGain(gain: Int): Boolean {
        val path = pathResolver.getSoundControlPath() ?: return false
        return RootShellManager.execute("echo $gain > $path/headphone_gain") is RootResult.Success
    }

    override suspend fun getSoundGains(): Map<String, Int> {
        val path = pathResolver.getSoundControlPath() ?: return emptyMap()
        val speaker = RootShellManager.execute("cat $path/speaker_gain").let { if (it is RootResult.Success) it.output.trim().toIntOrNull() ?: 0 else 0 }
        val headphone = RootShellManager.execute("cat $path/headphone_gain").let { if (it is RootResult.Success) it.output.trim().toIntOrNull() ?: 0 else 0 }
        return mapOf("speaker" to speaker, "headphone" to headphone)
    }

    override suspend fun readSysfs(path: String): String {
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim() else "Error reading path"
    }

    override suspend fun writeSysfs(path: String, value: String): Boolean {
        return RootShellManager.execute("echo $value > $path") is RootResult.Success
    }
}
