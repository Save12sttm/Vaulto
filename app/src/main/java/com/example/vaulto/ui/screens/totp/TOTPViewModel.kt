package com.example.vaulto.ui.screens.totp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.data.local.entities.VaultItemEntity
import com.example.vaulto.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TOTPViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TOTPState())
    val state: StateFlow<TOTPState> = _state.asStateFlow()

    fun loadTOTPItems() {
        viewModelScope.launch {
            repository.getAllItems()
                .map { items -> items.filter { it.hasTOTP && it.totpSecret.isNotEmpty() } }
                .collect { totpItems ->
                    _state.update { it.copy(items = totpItems) }
                }
        }
    }

    fun addTOTPCode(title: String, username: String, secret: String) {
        viewModelScope.launch {
            try {
                val newItem = VaultItemEntity(
                    title = title,
                    username = username,
                    password = "", // No password needed for TOTP-only entries
                    totpSecret = secret.replace(" ", "").uppercase(),
                    hasTOTP = true,
                    category = "Authenticator",
                    notes = "2FA Authenticator Code"
                )
                repository.insertItem(newItem)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

data class TOTPState(
    val items: List<VaultItemEntity> = emptyList()
)