package com.ext.kernelmanager.domain.repository

interface BatteryRepository {
    // profileName can be "Battery Saver", "Balanced", "Performance"
    suspend fun applyProfile(profileName: String): Boolean
    suspend fun getCurrentProfile(): String
}
