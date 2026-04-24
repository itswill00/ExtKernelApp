package com.ext.kernelmanager.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ext.kernelmanager.data.local.SettingsRepository
import com.ext.kernelmanager.domain.repository.CpuRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@HiltWorker
class ApplyOnBootWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val cpuRepository: CpuRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BootWorker", "Starting kernel parameter application...")
        
        // SAFE MODE: Delay execution
        // Wait for the system to stabilize (45 seconds) before modifying sensitive parameters.
        delay(45000)

        return try {
            val savedGovernor = settingsRepository.preferredGovernor.first()
            
            if (savedGovernor != null) {
                Log.d("BootWorker", "Applying Governor: $savedGovernor")
                val success = cpuRepository.setGovernor(savedGovernor)
                if (success) Result.success() else Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("BootWorker", "Failed to apply parameters", e)
            Result.failure()
        }
    }
}
