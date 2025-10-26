package com.example.vaulto.ui.screens.auth

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    val isBiometricAvailable: Boolean = false
)

enum class PasswordStrength {
    WEAK, FAIR, GOOD, STRONG, VERY_STRONG
}