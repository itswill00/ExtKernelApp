package com.ext.kernelmanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface FlasherRepository {
    // Validates a file size, extension (.sh, .zip, .img)
    suspend fun validateFile(filePath: String): Boolean
    
    // Executes a script or command and emits log lines in real-time
    fun executeWithProgress(command: String): Flow<String>
}
