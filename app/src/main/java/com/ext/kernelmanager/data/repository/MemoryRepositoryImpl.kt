package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.domain.repository.MemoryRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor() : MemoryRepository {

    override suspend fun isZramSupported(): Boolean {
        return File("/sys/block/zram0").exists()
    }

    override suspend fun getZramSizeMb(): Int {
        val result = RootShellManager.execute("cat /sys/block/zram0/disksize")
        if (result is RootResult.Success) {
            val bytes = result.output.trim().toLongOrNull() ?: 0L
            return (bytes / (1024 * 1024)).toInt()
        }
        return 0
    }

    override suspend fun setZramSizeMb(sizeMb: Int): Boolean {
        if (!isZramSupported()) return false
        val bytes = sizeMb.toLong() * 1024 * 1024
        
        // ZRAM must be reset before changing size
        val command = """
            swapoff /dev/block/zram0
            echo 1 > /sys/block/zram0/reset
            echo $bytes > /sys/block/zram0/disksize
            mkswap /dev/block/zram0
            swapon /dev/block/zram0
        """.trimIndent()
        
        val result = RootShellManager.execute(command)
        return result is RootResult.Success
    }

    override suspend fun getSwappiness(): Int {
        val result = RootShellManager.execute("cat /proc/sys/vm/swappiness")
        if (result is RootResult.Success) {
            return result.output.trim().toIntOrNull() ?: 60
        }
        return 60
    }

    override suspend fun setSwappiness(value: Int): Boolean {
        val result = RootShellManager.execute("echo $value > /proc/sys/vm/swappiness")
        return result is RootResult.Success
    }

    override suspend fun getCurrentLmkProfile(): String {
        // This is a simplification. Real LMK reading involves parsing /sys/module/lowmemorykiller/parameters/minfree
        // To keep it simple and stable, we'll return "Balanced" as default
        return "Balanced"
    }

    override suspend fun setLmkProfile(profileName: String): Boolean {
        // LMK values (pages). Each page is 4KB.
        // Example: 18432,23040,27648,32256,55296,80640 (Balanced)
        val minfreePath = "/sys/module/lowmemorykiller/parameters/minfree"
        
        val values = when (profileName) {
            "Aggressive" -> "18432,23040,27648,32256,110592,161280"
            "Light" -> "18432,23040,27648,32256,41472,60480"
            else -> "18432,23040,27648,32256,55296,80640" // Balanced
        }
        
        val result = RootShellManager.execute("echo $values > $minfreePath")
        return result is RootResult.Success
    }
}
