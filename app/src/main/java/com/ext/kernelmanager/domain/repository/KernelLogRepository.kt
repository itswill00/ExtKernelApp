package com.ext.kernelmanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface KernelLogRepository {
    /**
     * Reads dmesg logs in real-time.
     */
    fun getKernelLogs(): Flow<String>

    /**
     * Reads system logcat with a specific filter.
     */
    fun getSystemLogs(filter: String = ""): Flow<String>
}
