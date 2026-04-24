package com.ext.kernelmanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface KernelLogRepository {
    /**
     * Membaca log dmesg secara real-time.
     */
    fun getKernelLogs(): Flow<String>

    /**
     * Membaca logcat sistem dengan filter tertentu.
     */
    fun getSystemLogs(filter: String = ""): Flow<String>
}
