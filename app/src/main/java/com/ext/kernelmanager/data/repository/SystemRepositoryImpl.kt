package com.ext.kernelmanager.data.repository

import com.ext.kernelmanager.core.hardware.DeviceIdentity
import com.ext.kernelmanager.core.hardware.HardwareDetector
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.domain.repository.SystemRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepositoryImpl @Inject constructor(
    private val hardwareDetector: HardwareDetector
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

    override suspend fun isRootAvailable(): Boolean {
        return RootShellManager.isRootAvailable()
    }
}
