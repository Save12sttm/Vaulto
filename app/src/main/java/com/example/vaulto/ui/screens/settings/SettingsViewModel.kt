package com.example.vaulto.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.isBiometricEnabled.collect { enabled ->
                _state.update { it.copy(biometricEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesManager.autoLockTimeout.collect { timeout ->
                _state.update { 
                    it.copy(
                        autoLockTimeout = timeout,
                        autoLockLabel = getAutoLockLabel(timeout)
                    )
                }
            }
        }
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            val newValue = !_state.value.biometricEnabled
            preferencesManager.setBiometricEnabled(newValue)
            _state.update { it.copy(biometricEnabled = newValue) }
        }
    }

    fun setAutoLockTimeout(milliseconds: Int) {
        viewModelScope.launch {
            preferencesManager.setAutoLockTimeout(milliseconds)
            _state.update { 
                it.copy(
                    autoLockTimeout = milliseconds,
                    autoLockLabel = getAutoLockLabel(milliseconds)
                )
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(theme)
            _state.update { it.copy(theme = theme, themeLabel = theme) }
        }
    }

    fun toggleDynamicColors() {
        _state.update { it.copy(dynamicColors = !it.dynamicColors) }
    }

    fun toggleShowPasswordOnCopy() {
        _state.update { it.copy(showPasswordOnCopy = !it.showPasswordOnCopy) }
    }

    private fun getAutoLockLabel(milliseconds: Int): String {
        return when (milliseconds) {
            0 -> "Immediately"
            30000 -> "30 seconds"
            60000 -> "1 minute"
            300000 -> "5 minutes"
            900000 -> "15 minutes"
            -1 -> "Never"
            else -> "$milliseconds ms"
        }
    }
}

data class SettingsState(
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: Int = 300000,
    val autoLockLabel: String = "5 minutes",
    val theme: String = "System",
    val themeLabel: String = "System",
    val dynamicColors: Boolean = true,
    val clipboardTimeout: Int = 30,
    val showPasswordOnCopy: Boolean = false
)