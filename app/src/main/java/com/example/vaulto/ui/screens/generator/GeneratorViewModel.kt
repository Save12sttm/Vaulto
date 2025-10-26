package com.example.vaulto.ui.screens.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.util.crypto.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log2

@HiltViewModel
class GeneratorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    private val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
    private val numberChars = "0123456789"
    private val symbolChars = "!@#$%^&*()_+-=[]{}|;:,.<?>"
    private val ambiguousChars = "il1Lo0O"

    init {
        generatePassword()
    }

    fun generatePassword() {
        viewModelScope.launch {
            val state = _state.value
            
            val pool = buildCharacterPool(
                useUppercase = state.useUppercase,
                useLowercase = state.useLowercase,
                useNumbers = state.useNumbers,
                useSymbols = state.useSymbols,
                excludeAmbiguous = state.excludeAmbiguous
            )

            if (pool.isEmpty()) {
                _state.update { it.copy(generatedPassword = "") }
                return@launch
            }

            val password = generateSecurePassword(pool, state.length)
            val entropy = calculateEntropy(pool.length, state.length)
            val strength = PasswordStrength.fromEntropy(entropy)
            val newHistory = (listOf(password) + state.generationHistory).take(10)

            _state.update {
                it.copy(
                    generatedPassword = password,
                    entropy = entropy,
                    passwordStrength = strength,
                    generationHistory = newHistory
                )
            }
        }
    }

    private fun buildCharacterPool(
        useUppercase: Boolean,
        useLowercase: Boolean,
        useNumbers: Boolean,
        useSymbols: Boolean,
        excludeAmbiguous: Boolean
    ): String {
        val pool = StringBuilder()
        
        if (useUppercase) pool.append(uppercaseChars)
        if (useLowercase) pool.append(lowercaseChars)
        if (useNumbers) pool.append(numberChars)
        if (useSymbols) pool.append(symbolChars)

        var result = pool.toString()
        
        if (excludeAmbiguous) {
            result = result.filterNot { it in ambiguousChars }
        }

        return result
    }

    private fun generateSecurePassword(pool: String, length: Int): String {
        val randomBytes = CryptoManager.generateSecureRandom(length)
        val password = StringBuilder()

        for (i in 0 until length) {
            val index = (randomBytes[i].toInt() and 0xFF) % pool.length
            password.append(pool[index])
        }

        return password.toString()
    }

    private fun calculateEntropy(poolSize: Int, length: Int): Double {
        if (poolSize == 0 || length == 0) return 0.0
        return length * log2(poolSize.toDouble())
    }

    fun updateLength(length: Int) {
        _state.update { it.copy(length = length.coerceIn(4, 128)) }
        generatePassword()
    }

    fun toggleUppercase() {
        _state.update { it.copy(useUppercase = !it.useUppercase) }
        generatePassword()
    }

    fun toggleLowercase() {
        _state.update { it.copy(useLowercase = !it.useLowercase) }
        generatePassword()
    }

    fun toggleNumbers() {
        _state.update { it.copy(useNumbers = !it.useNumbers) }
        generatePassword()
    }

    fun toggleSymbols() {
        _state.update { it.copy(useSymbols = !it.useSymbols) }
        generatePassword()
    }

    fun toggleExcludeAmbiguous() {
        _state.update { it.copy(excludeAmbiguous = !it.excludeAmbiguous) }
        generatePassword()
    }

    fun restoreFromHistory(password: String) {
        val entropy = calculateEntropy(
            buildCharacterPool(
                _state.value.useUppercase,
                _state.value.useLowercase,
                _state.value.useNumbers,
                _state.value.useSymbols,
                _state.value.excludeAmbiguous
            ).length,
            password.length
        )
        val strength = PasswordStrength.fromEntropy(entropy)

        _state.update {
            it.copy(
                generatedPassword = password,
                entropy = entropy,
                passwordStrength = strength,
                length = password.length
            )
        }
    }
}