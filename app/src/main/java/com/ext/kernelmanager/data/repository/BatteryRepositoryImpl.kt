package com.ext.kernelmanager.data.repository

import android.content.Context
import com.ext.kernelmanager.core.root.RootResult
import com.ext.kernelmanager.core.root.RootShellManager
import com.ext.kernelmanager.domain.repository.BatteryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryRepository {

    private val PREFS_NAME = "battery_prefs"
    private val KEY_PROFILE = "current_profile"

    override suspend fun applyProfile(profileName: String): Boolean = withContext(Dispatchers.IO) {
        val command = when (profileName) {
            "Battery Saver" -> """
                # Limit max freq, use conservative/powersave governor
                for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo powersave > ${'$'}i; done
                # Enable multi-core power savings
                echo 2 > /sys/devices/system/cpu/sched_mc_power_savings 2>/dev/null || true
            """.trimIndent()

            "Performance" -> """
                # Maximize freq, use performance governor
                for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > ${'$'}i; done
                # Disable multi-core power savings
                echo 0 > /sys/devices/system/cpu/sched_mc_power_savings 2>/dev/null || true
            """.trimIndent()

            else -> """ // "Balanced"
                # Use schedutil/interactive/ondemand
                for i in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > ${'$'}i; done
                # Balanced multi-core power savings
                echo 1 > /sys/devices/system/cpu/sched_mc_power_savings 2>/dev/null || true
            """.trimIndent()
        }

        val result = RootShellManager.execute(command)
        if (result is RootResult.Success) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PROFILE, profileName).apply()
            return@withContext true
        }
        return@withContext false
    }

    override suspend fun getCurrentProfile(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE, "Balanced") ?: "Balanced"
    }
}
