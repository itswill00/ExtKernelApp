package com.ext.kernelmanager.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val GOVERNOR_KEY = stringPreferencesKey("cpu_governor")

    val preferredGovernor: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOVERNOR_KEY]
    }

    suspend fun saveGovernor(governor: String) {
        context.dataStore.edit { preferences ->
            preferences[GOVERNOR_KEY] = governor
        }
    }
}
