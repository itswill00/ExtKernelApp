package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.domain.repository.CpuRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CpuRepositoryImpl @Inject constructor() : CpuRepository {

    // Helper untuk mencari path yang benar secara dinamis
    private suspend fun findValidPath(paths: List<String>): String? {
        for (path in paths) {
            if (File(path).exists()) return path
        }
        return null
    }

    override suspend fun getAvailableGovernors(): List<String> {
        val paths = listOf(
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
            "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors"
        )
        val path = findValidPath(paths) ?: return emptyList()
        
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) {
            result.output.trim().split(" ").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    override suspend fun getCurrentGovernor(): String {
        val paths = listOf(
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
            "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor"
        )
        val path = findValidPath(paths) ?: return "Tidak Terdeteksi"
        
        val result = RootShellManager.execute("cat $path")
        return if (result is RootResult.Success) result.output.trim() else "N/A"
    }

    override suspend fun setGovernor(governor: String): Boolean {
        return try {
            // Mencoba set untuk semua core/policy
            val command = """
                for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo $governor > ${'$'}i; done
                for i in /sys/devices/system/cpu/cpufreq/policy*/scaling_governor; do echo $governor > ${'$'}i; done
            """.trimIndent()
            
            val result = RootShellManager.execute(command)
            result is RootResult.Success
        } catch (e: Exception) {
            false
        }
    }
}
