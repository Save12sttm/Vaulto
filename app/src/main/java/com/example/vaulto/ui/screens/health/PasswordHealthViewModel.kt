package com.example.vaulto.ui.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.data.local.entities.VaultItemEntity
import com.example.vaulto.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordHealthViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordHealthState())
    val state: StateFlow<PasswordHealthState> = _state.asStateFlow()

    fun analyzePasswords() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val allItems = repository.getAllItems().first()
                
                val weakPasswords = mutableListOf<VaultItemEntity>()
                val reusedPasswords = mutableListOf<VaultItemEntity>()
                val oldPasswords = mutableListOf<VaultItemEntity>()
                
                // Check for weak passwords
                allItems.forEach { item ->
                    val strength = calculatePasswordStrength(item.password)
                    if (strength < 40) {
                        weakPasswords.add(item)
                    }
                }
                
                // Check for reused passwords
                val passwordGroups = allItems.groupBy { it.password }
                passwordGroups.forEach { (password, items) ->
                    if (items.size > 1 && password.isNotEmpty()) {
                        reusedPasswords.addAll(items)
                    }
                }
                
                // Check for old passwords (>90 days)
                val ninetyDaysAgo = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
                allItems.forEach { item ->
                    if (item.modifiedAt < ninetyDaysAgo) {
                        oldPasswords.add(item)
                    }
                }
                
                // Calculate security score
                val totalIssues = weakPasswords.size + reusedPasswords.size + oldPasswords.size
                val totalPasswords = allItems.size.coerceAtLeast(1)
                val issueRatio = totalIssues.toFloat() / totalPasswords
                val score = ((1 - issueRatio) * 100).toInt().coerceIn(0, 100)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    totalPasswords = allItems.size,
                    weakPasswords = weakPasswords,
                    reusedPasswords = reusedPasswords.distinctBy { it.id },
                    oldPasswords = oldPasswords,
                    securityScore = score
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun calculatePasswordStrength(password: String): Int {
        if (password.isEmpty()) return 0
        
        var score = 0
        
        // Length
        when {
            password.length >= 16 -> score += 30
            password.length >= 12 -> score += 20
            password.length >= 8 -> score += 10
        }
        
        // Character variety
        if (password.any { it.isUpperCase() }) score += 15
        if (password.any { it.isLowerCase() }) score += 15
        if (password.any { it.isDigit() }) score += 15
        if (password.any { !it.isLetterOrDigit() }) score += 25
        
        return score.coerceIn(0, 100)
    }
}

data class PasswordHealthState(
    val isLoading: Boolean = false,
    val totalPasswords: Int = 0,
    val weakPasswords: List<VaultItemEntity> = emptyList(),
    val reusedPasswords: List<VaultItemEntity> = emptyList(),
    val oldPasswords: List<VaultItemEntity> = emptyList(),
    val securityScore: Int = 100,
    val errorMessage: String? = null
)