package com.ext.kernelmanager.domain.model.telemetry

/**
 * Massive Data Model for System Telemetry.
 * Designed for thousands of lines of logic handling every hardware aspect.
 */

data class SystemTelemetry(
    val cpu: CpuTelemetry,
    val gpu: GpuTelemetry,
    val memory: MemoryTelemetry,
    val battery: BatteryTelemetry,
    val thermal: List<ThermalTelemetry>,
    val kernel: KernelTelemetry,
    val uptime: String
)

data class CpuTelemetry(
    val totalLoad: Int,
    val cores: List<CpuCoreTelemetry>,
    val clusters: List<CpuClusterTelemetry>
)

data class CpuCoreTelemetry(
    val id: Int,
    val currentFreq: String,
    val isOnline: Boolean,
    val load: Int
)

data class CpuClusterTelemetry(
    val id: Int,
    val governor: String,
    val minFreq: String,
    val maxFreq: String
)

data class GpuTelemetry(
    val currentFreq: String,
    val load: Int,
    val governor: String
)

data class MemoryTelemetry(
    val total: Long,
    val free: Long,
    val available: Long,
    val cached: Long,
    val buffers: Long,
    val swapTotal: Long,
    val swapFree: Long,
    val zramSize: Long,
    val usagePercent: Float
)

data class BatteryTelemetry(
    val percentage: Int,
    val health: String,
    val status: String,
    val voltage: Float, // in Volts
    val current: Int,   // in mA
    val temperature: Float,
    val technology: String,
    val capacityAh: Int
)

data class ThermalTelemetry(
    val zone: String,
    val temperature: Float,
    val type: String
)

data class KernelTelemetry(
    val version: String,
    val compiler: String,
    val architecture: String,
    val buildDate: String
)
