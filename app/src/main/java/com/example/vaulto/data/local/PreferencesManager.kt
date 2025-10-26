package com.example.vaulto.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vaulto_preferences")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val MASTER_PASSWORD_HASH = stringPreferencesKey("master_password_hash")
        val PASSWORD_SALT = stringPreferencesKey("password_salt")
        val CLIPBOARD_CLEAR_TIMEOUT = intPreferencesKey("clipboard_clear_timeout")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
    
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }
    
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }
    
    val autoLockTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_LOCK_TIMEOUT] ?: 300000
    }
    
    val clipboardClearTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_CLEAR_TIMEOUT] ?: 30000
    }
    
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }
    
    suspend fun setAutoLockTimeout(milliseconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_LOCK_TIMEOUT] = milliseconds
        }
    }
    
    suspend fun saveMasterPasswordHash(hash: String, salt: String) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_PASSWORD_HASH] = hash
            preferences[PASSWORD_SALT] = salt
        }
    }
    
    suspend fun getMasterPasswordHash(): Pair<String?, String?> {
        val preferences = context.dataStore.data.first()
        val hash = preferences[MASTER_PASSWORD_HASH]
        val salt = preferences[PASSWORD_SALT]
        return Pair(hash, salt)
    }
    
    suspend fun setClipboardClearTimeout(milliseconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[CLIPBOARD_CLEAR_TIMEOUT] = milliseconds
        }
    }
    
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
}