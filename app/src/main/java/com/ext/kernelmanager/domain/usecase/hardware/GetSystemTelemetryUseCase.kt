package com.ext.kernelmanager.domain.usecase.hardware

import com.ext.kernelmanager.domain.manager.MasterHardwareManager
import com.ext.kernelmanager.domain.model.telemetry.SystemTelemetry
import javax.inject.Inject

/**
 * GetSystemTelemetryUseCase:
 * Fetches the entire hardware telemetry snapshot.
 * This is the entry point for the Dashboard UI.
 */
class GetSystemTelemetryUseCase @Inject constructor(
    private val hardwareManager: MasterHardwareManager
) {
    suspend fun execute(): SystemTelemetry {
        return hardwareManager.collectFullTelemetry()
    }
}
