package com.ext.kernelmanager.domain.repository

import com.ext.kernelmanager.core.sysfs.CpuCluster
import com.ext.kernelmanager.core.sysfs.GpuInfo

interface HardcoreTuningRepository {

    // CPU (Per Cluster)
    suspend fun getCpuClusters(): List<CpuCluster>
    suspend fun setClusterGovernor(clusterId: Int, governor: String): Boolean
    suspend fun setClusterMinFreq(clusterId: Int, freq: Long): Boolean
    suspend fun setClusterMaxFreq(clusterId: Int, freq: Long): Boolean
    suspend fun getClusterCurrentFreq(clusterId: Int): Long
    suspend fun getClusterCurrentGovernor(clusterId: Int): String
    
    // CPU Advanced: Core control
    suspend fun setCoreOnline(coreId: Int, online: Boolean): Boolean
    suspend fun isCoreOnline(coreId: Int): Boolean

    // GPU
    suspend fun getGpuInfo(): GpuInfo?
    suspend fun setGpuGovernor(governor: String): Boolean
    suspend fun setGpuMaxFreq(freq: Long): Boolean
    suspend fun getGpuCurrentFreq(): Long
    suspend fun getGpuCurrentGovernor(): String

    // I/O Scheduler
    suspend fun getBlockDevices(): List<String>
    suspend fun getAvailableIoSchedulers(device: String): List<String>
    suspend fun getCurrentIoScheduler(device: String): String
    suspend fun setIoScheduler(device: String, scheduler: String): Boolean
    suspend fun setReadAhead(device: String, kb: Int): Boolean
    suspend fun getReadAhead(device: String): Int
    
    // Advanced Sysfs generic
    suspend fun readSysfs(path: String): String
    suspend fun writeSysfs(path: String, value: String): Boolean
}
