package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.domain.repository.KernelLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KernelLogRepositoryImpl @Inject constructor() : KernelLogRepository {

    override fun getKernelLogs(): Flow<String> = flow {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "dmesg -w"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line ?: "")
            }
        } catch (e: Exception) {
            emit("Error reading dmesg: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    override fun getSystemLogs(filter: String): Flow<String> = flow {
        try {
            val command = if (filter.isEmpty()) "logcat" else "logcat *:$filter"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line ?: "")
            }
        } catch (e: Exception) {
            emit("Error reading logcat: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
