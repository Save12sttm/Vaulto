package com.example.vaulto.ui.screens.auth

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.data.local.PreferencesManager
import com.example.vaulto.util.crypto.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun checkPasswordStrength(password: String) {
        val strength = when {
            password.length < 8 -> PasswordStrength.WEAK
            password.length < 12 -> PasswordStrength.FAIR
            password.length < 16 && hasVariedCharacters(password) -> PasswordStrength.GOOD
            password.length < 20 && hasVariedCharacters(password) -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
        _state.value = _state.value.copy(passwordStrength = strength)
    }

    private fun hasVariedCharacters(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return listOf(hasUppercase, hasLowercase, hasDigit, hasSpecial).count { it } >= 3
    }

    fun setupMasterPassword(password: String, confirmPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            if (password != confirmPassword) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Passwords do not match"
                )
                return@launch
            }

            if (password.length < 8) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Password must be at least 8 characters"
                )
                return@launch
            }

            try {
                val salt = CryptoManager.generateSalt()
                val hash = CryptoManager.hashPassword(password, salt)

                val hashString = Base64.encodeToString(hash, Base64.NO_WRAP)
                val saltString = Base64.encodeToString(salt, Base64.NO_WRAP)
                preferencesManager.saveMasterPasswordHash(hashString, saltString)
                preferencesManager.setFirstLaunchComplete()

                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to setup password: ${e.message}"
                )
            }
        }
    }

    fun verifyMasterPassword(password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val (hashString, saltString) = preferencesManager.getMasterPasswordHash()
                
                if (hashString == null || saltString == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No master password set"
                    )
                    return@launch
                }

                val storedHash = Base64.decode(hashString, Base64.NO_WRAP)
                val salt = Base64.decode(saltString, Base64.NO_WRAP)

                val isValid = CryptoManager.verifyPassword(password, storedHash, salt)

                if (isValid) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Incorrect password"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    fun setError(message: String) {
        _state.value = _state.value.copy(errorMessage = message)
    }
    
    fun authenticateWithBiometric() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = false,
                isAuthenticated = true
            )
        }
    }
}