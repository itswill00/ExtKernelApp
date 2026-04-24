package com.ext.kernelmanager.core.discovery

import android.util.Log
import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SysfsScanner: The intelligence hub of the application.
 * This engine crawls sysfs directories recursively to discover hardware nodes.
 * It uses pattern matching and attribute probing to ensure zero hardcoding.
 */
@Singleton
class SysfsScanner @Inject constructor() {

    private val TAG = "SysfsScanner"

    /**
     * Probes the system for nodes matching a specific signature.
     * @param baseDir The root directory to start scanning.
     * @param fileNamePattern The regex to match filenames.
     * @param maxDepth How deep the recursion should go.
     */
    suspend fun discoverNodes(
        baseDir: String,
        fileNamePattern: String,
        maxDepth: Int = 3
    ): List<String> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<String>()
        val pattern = Pattern.compile(fileNamePattern)
        
        try {
            // Using 'find' via root shell is much faster than JVM File recursion for sysfs
            val command = "find $baseDir -maxdepth $maxDepth -name \"*\""
            val result = RootShellManager.execute(command)
            
            if (result is RootResult.Success) {
                result.output.split("\n").forEach { path ->
                    val file = File(path)
                    if (pattern.matcher(file.name).matches()) {
                        discovered.add(path)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Discovery failed in $baseDir", e)
        }
        
        discovered.distinct().sorted()
    }

    /**
     * Probes a directory for a set of known attributes.
     * Used to identify if a directory represents a CPU, GPU, or Battery.
     */
    suspend fun probeAttributes(dirPath: String, attributes: List<String>): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Boolean>()
        attributes.forEach { attr ->
            results[attr] = File(dirPath, attr).exists()
        }
        results
    }

    /**
     * Identifies the primary block device for the data partition.
     */
    suspend fun findPrimaryBlockDevice(): String? = withContext(Dispatchers.IO) {
        val result = RootShellManager.execute("mount | grep ' /data '")
        if (result is RootResult.Success) {
            val dev = result.output.split(" ")[0]
            if (dev.startsWith("/dev/block/")) {
                // Resolve symlinks (e.g., /dev/block/bootdevice/by-name/userdata -> /dev/block/sdaXX)
                val resolve = RootShellManager.execute("readlink -f $dev")
                if (resolve is RootResult.Success) return@withContext resolve.output.trim()
                return@withContext dev
            }
        }
        null
    }
}

/**
 * NodeSignature: Defines what a specific hardware node looks like.
 */
data class NodeSignature(
    val type: HardwareType,
    val searchPaths: List<String>,
    val filePattern: String,
    val requiredAttributes: List<String>
)

enum class HardwareType {
    CPU_CORE,
    CPU_CLUSTER,
    GPU,
    BATTERY,
    THERMAL_ZONE,
    I_O_SCHEDULER,
    DISK,
    ZRAM
}
