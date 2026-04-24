package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.hardware.DeviceIdentity
import com.ext.kernelmanager.core.hardware.HardwareDetector
import com.ext.kernelmanager.core.hardware.AdvancedHardwareDetector
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.domain.model.telemetry.GpuTelemetry
import com.ext.kernelmanager.domain.model.telemetry.SystemTelemetry
import com.ext.kernelmanager.domain.repository.SystemRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepositoryImpl @Inject constructor(
    private val hardwareDetector: HardwareDetector,
    private val advancedDetector: AdvancedHardwareDetector
) : SystemRepository {

    override fun getDeviceIdentity(): DeviceIdentity {
        return hardwareDetector.getDeviceInfo()
    }

    override suspend fun getCpuFrequency(coreIndex: Int): String {
        return hardwareDetector.getCpuMaxFreq(coreIndex)
    }

    override suspend fun getTemperature(): String {
        return hardwareDetector.getDeviceTemp()
    }

    override suspend fun getRamUsage(): Pair<Long, Long> {
        return hardwareDetector.getRamStatus()
    }

    override suspend fun getBatteryInfo(): Pair<Int, String> {
        return hardwareDetector.getBatteryInfo()
    }

    override suspend fun getUptime(): String {
        return hardwareDetector.getUptime()
    }

    override suspend fun isRootAvailable(): Boolean {
        return RootShellManager.isRootAvailable()
    }

    override suspend fun getFullTelemetry(): SystemTelemetry {
        return SystemTelemetry(
            cpu = advancedDetector.getCpuTelemetry(),
            gpu = GpuTelemetry("N/A", 0, "N/A"),
            memory = advancedDetector.getMemoryTelemetry(),
            battery = advancedDetector.getBatteryTelemetry(),
            thermal = advancedDetector.getThermalTelemetry(),
            kernel = advancedDetector.getKernelTelemetry(),
            uptime = hardwareDetector.getUptime()
        )
    }
}
