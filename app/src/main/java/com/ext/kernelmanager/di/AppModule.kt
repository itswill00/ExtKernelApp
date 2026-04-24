package com.ext.kernelmanager.di

import com.ext.kernelmanager.core.hardware.HardwareDetector
import com.ext.kernelmanager.data.repository.SystemRepositoryImpl
import com.ext.kernelmanager.domain.repository.SystemRepository
import com.ext.kernelmanager.domain.repository.CpuRepository
import com.ext.kernelmanager.domain.repository.MemoryRepository
import com.ext.kernelmanager.domain.repository.BatteryRepository
import com.ext.kernelmanager.domain.repository.FlasherRepository
import com.ext.kernelmanager.domain.repository.HardcoreTuningRepository
import com.ext.kernelmanager.domain.repository.KernelLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHardwareDetector(): HardwareDetector {
        return HardwareDetector()
    }

    @Provides
    @Singleton
    fun provideAdvancedHardwareDetector(
        pathResolver: com.ext.kernelmanager.core.sysfs.SysfsPathResolver
    ): com.ext.kernelmanager.core.hardware.AdvancedHardwareDetector {
        return com.ext.kernelmanager.core.hardware.AdvancedHardwareDetector(pathResolver)
    }

    @Provides
    @Singleton
    fun provideSystemRepository(
        hardwareDetector: HardwareDetector,
        advancedDetector: com.ext.kernelmanager.core.hardware.AdvancedHardwareDetector
    ): SystemRepository {
        return com.ext.kernelmanager.data.repository.SystemRepositoryImpl(hardwareDetector, advancedDetector)
    }

    @Provides
    @Singleton
    fun provideCpuRepository(): CpuRepository {
        return com.ext.kernelmanager.data.repository.CpuRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideMemoryRepository(): MemoryRepository {
        return com.ext.kernelmanager.data.repository.MemoryRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideBatteryRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): BatteryRepository {
        return com.ext.kernelmanager.data.repository.BatteryRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideFlasherRepository(): FlasherRepository {
        return com.ext.kernelmanager.data.repository.FlasherRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSysfsPathResolver(): com.ext.kernelmanager.core.sysfs.SysfsPathResolver {
        return com.ext.kernelmanager.core.sysfs.SysfsPathResolver()
    }

    @Provides
    @Singleton
    fun provideHardcoreTuningRepository(
        pathResolver: com.ext.kernelmanager.core.sysfs.SysfsPathResolver
    ): HardcoreTuningRepository {
        return com.ext.kernelmanager.data.repository.HardcoreTuningRepositoryImpl(pathResolver)
    }

    @Provides
    @Singleton
    fun provideKernelLogRepository(): KernelLogRepository {
        return com.ext.kernelmanager.data.repository.KernelLogRepositoryImpl()
    }
}
