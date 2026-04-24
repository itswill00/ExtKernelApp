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
        Log.d("BootWorker", "Memulai penerapan parameter kernel...")
        
        // FAIL-SAFE 1: Penundaan eksekusi (Safe Mode)
        // Kita menunggu sistem stabil (30-60 detik) sebelum memodifikasi parameter sensitif.
        delay(45000)

        return try {
            val savedGovernor = settingsRepository.preferredGovernor.first()
            
            if (savedGovernor != null) {
                Log.d("BootWorker", "Menerapkan Governor: $savedGovernor")
                val success = cpuRepository.setGovernor(savedGovernor)
                if (success) Result.success() else Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("BootWorker", "Gagal menerapkan parameter", e)
            Result.failure()
        }
    }
}
