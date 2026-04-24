package com.ext.kernelmanager.core.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * RootShellManager: Handles root shell command execution securely.
 * Focused on safety and human-readable error reporting.
 */
object RootShellManager {

    /**
     * Checks if root access is available and granted by the user.
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
     * Executes single or multiple shell commands.
     * Returns a Result containing output or a human-readable error message.
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
                RootResult.Failure("Failed to adjust system: $error")
            }
        } catch (e: Exception) {
            RootResult.Error("Encountered an issue while accessing system privileges. Ensure Root permission is granted.")
        }
    }
}

sealed class RootResult {
    data class Success(val output: String) : RootResult()
    data class Failure(val message: String) : RootResult()
    data class Error(val message: String) : RootResult()
}
