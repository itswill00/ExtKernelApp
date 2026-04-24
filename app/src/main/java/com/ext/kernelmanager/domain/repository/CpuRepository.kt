package com.ext.kernelmanager.domain.repository

interface CpuRepository {
    suspend fun getAvailableGovernors(): List<String>
    suspend fun getCurrentGovernor(): String
    suspend fun setGovernor(governor: String): Boolean
}
