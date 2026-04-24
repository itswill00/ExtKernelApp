package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.domain.repository.FlasherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlasherRepositoryImpl @Inject constructor() : FlasherRepository {

    override suspend fun validateFile(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false
        
        // Logical checks
        val extension = file.extension.lowercase()
        val validExtensions = listOf("sh", "zip", "img")
        if (!validExtensions.contains(extension)) return false
        
        // Size validation (example: max 100MB for .img/zip, 5MB for .sh)
        val sizeMb = file.length() / (1024 * 1024)
        if (extension == "sh" && sizeMb > 5) return false
        if ((extension == "zip" || extension == "img") && sizeMb > 150) return false
        
        return true
    }

    override fun executeWithProgress(command: String): Flow<String> = flow {
        try {
            emit(">> Memulai eksekusi perintah keamanan tingkat tinggi...")
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            // Menggabungkan stdout dan stderr (karena `su` kadang memisahkan output log root)
            // Di sini kita membaca keduanya. Cara paling aman untuk bash adalah menambahkan 2>&1
            val fullCommand = "$command 2>&1\n"
            os.writeBytes(fullCommand)
            os.writeBytes("exit\n")
            os.flush()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line ?: "")
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                emit(">> Eksekusi selesai dengan status: Sukses.")
            } else {
                emit(">> Eksekusi dihentikan dengan pesan error (Kode $exitCode).")
                // Membaca error jika ada sisa (sebenarnya sudah digabung ke 2>&1)
                while (errorReader.readLine().also { line = it } != null) {
                    emit("Error: $line")
                }
            }

        } catch (e: Exception) {
            emit(">> Terjadi kesalahan sistem yang fatal: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
