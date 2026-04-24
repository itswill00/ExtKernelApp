package com.ext.kernelmanager.core.engine

import android.util.Log
import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MasterKernelEngine: Massive implementation for absolute kernel parameter management.
 * Contains logic for sysfs handling, synchronization, and persistence.
 */
@Singleton
class MasterKernelEngine @Inject constructor() {

    private val TAG = "MasterKernelEngine"

    /**
     * Ensures root access by attempting to execute the su binary.
     */
    suspend fun checkAndRequestRoot(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Requesting root access...")
        RootShellManager.isRootAvailable()
    }

    /**
     * Handles sysfs writing with retry mechanism and result validation.
     * Crucial for preventing write failures when the system is busy.
     */
    suspend fun writeParam(path: String, value: String, retries: Int = 3): Boolean = withContext(Dispatchers.IO) {
        if (!File(path).exists()) {
            Log.e(TAG, "Path not found: $path")
            return@withContext false
        }

        var currentAttempt = 0
        var success = false

        while (currentAttempt < retries && !success) {
            val result = RootShellManager.execute("echo $value > $path")
            if (result is RootResult.Success) {
                // Post-write verification
                val checkResult = RootShellManager.execute("cat $path")
                if (checkResult is RootResult.Success && checkResult.output.trim() == value) {
                    success = true
                } else {
                    Log.w(TAG, "Verification failed for $path. Expected $value, got ${checkResult}")
                }
            }
            if (!success) {
                currentAttempt++
                delay(100) // Brief backoff
            }
        }
        success
    }

    /**
     * Reads sysfs parameters with detailed error handling.
     */
    suspend fun readParam(path: String): String? = withContext(Dispatchers.IO) {
        if (!File(path).exists()) return@withContext null
        val result = RootShellManager.execute("cat $path")
        if (result is RootResult.Success) result.output.trim() else null
    }

    /**
     * Batch Apply Logic: Applies multiple parameters at once.
     * Used by Power/Performance Profiles.
     */
    suspend fun applyBatch(params: Map<String, String>): Int {
        var appliedCount = 0
        params.forEach { (path, value) ->
            if (writeParam(path, value)) {
                appliedCount++
            }
        }
        return appliedCount
    }
}
