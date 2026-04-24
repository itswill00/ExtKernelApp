package com.ext.kernelmanager.domain.repository

interface MemoryRepository {
    suspend fun isZramSupported(): Boolean
    suspend fun getZramSizeMb(): Int
    suspend fun setZramSizeMb(sizeMb: Int): Boolean
    
    suspend fun getSwappiness(): Int
    suspend fun setSwappiness(value: Int): Boolean
    
    // LMK profiles: "Aggressive", "Balanced", "Light"
    // Return empty string if not applicable or default
    suspend fun getCurrentLmkProfile(): String
    suspend fun setLmkProfile(profileName: String): Boolean
}
