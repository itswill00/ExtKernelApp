package com.ext.kernelmanager.core.node

import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * TunableNode: An abstraction over any sysfs/proc node that can be read or written.
 * This class ensures that every interaction is validated and safe.
 */
class TunableNode(
    val path: String,
    val description: String = "",
    val validator: (String) -> Boolean = { true }
) {

    /**
     * Reads the raw value from the node.
     */
    suspend fun read(): String = withContext(Dispatchers.IO) {
        if (!File(path).exists()) return@withContext "N/A"
        val result = RootShellManager.execute("cat $path")
        if (result is RootResult.Success) result.output.trim() else "Error"
    }

    /**
     * Writes a value to the node with validation and root privileges.
     */
    suspend fun write(value: String): Boolean = withContext(Dispatchers.IO) {
        if (!File(path).exists()) return@withContext false
        if (!validator(value)) return@withContext false
        
        val result = RootShellManager.execute("echo $value > $path")
        if (result is RootResult.Success) {
            // Verify write
            val verify = RootShellManager.execute("cat $path")
            return@withContext verify is RootResult.Success && verify.output.trim() == value
        }
        false
    }

    /**
     * Reads the value and converts it to an Int.
     */
    suspend fun readInt(defaultValue: Int = 0): Int {
        return read().toIntOrNull() ?: defaultValue
    }

    /**
     * Reads the value and converts it to a Long.
     */
    suspend fun readLong(defaultValue: Long = 0L): Long {
        return read().toLongOrNull() ?: defaultValue
    }
    
    /**
     * Returns true if the node exists and is accessible.
     */
    fun exists(): Boolean = File(path).exists()
}

/**
 * NodeGroup: A collection of nodes that act as a single hardware component (e.g., a CPU Cluster).
 */
abstract class NodeGroup(val basePath: String) {
    abstract fun isSupported(): Boolean
}
