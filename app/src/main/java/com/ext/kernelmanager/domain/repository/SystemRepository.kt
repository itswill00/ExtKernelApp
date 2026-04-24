package com.ext.kernelmanager.domain.repository

import com.ext.kernelmanager.core.hardware.DeviceIdentity

interface SystemRepository {
    fun getDeviceIdentity(): DeviceIdentity
    suspend fun getCpuFrequency(coreIndex: Int): String
    suspend fun getTemperature(): String
    suspend fun getRamUsage(): Pair<Long, Long>
    suspend fun getBatteryInfo(): Pair<Int, String>
    suspend fun getUptime(): String
    suspend fun isRootAvailable(): Boolean
}
