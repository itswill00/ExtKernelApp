package com.ext.kernelmanager.core.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Manager untuk menangani eksekusi perintah shell dengan hak akses Root.
 * Didesain dengan fokus pada keamanan dan penanganan error yang empatik.
 */
object RootShellManager {

    /**
     * Memeriksa apakah akses root tersedia dan diizinkan oleh pengguna.
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Mengeksekusi perintah shell tunggal atau jamak.
     * Mengembalikan Result dengan output atau pesan error yang bisa dipahami manusia.
     */
    suspend fun execute(command: String): RootResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            val output = reader.readLines().joinToString("\n")
            val error = errorReader.readLines().joinToString("\n")
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                RootResult.Success(output)
            } else {
                RootResult.Failure("Gagal menyesuaikan sistem: $error")
            }
        } catch (e: Exception) {
            RootResult.Error("Terjadi kendala saat mengakses sistem. Pastikan izin Root telah diberikan.")
        }
    }
}

sealed class RootResult {
    data class Success(val output: String) : RootResult()
    data class Failure(val message: String) : RootResult()
    data class Error(val message: String) : RootResult()
}
