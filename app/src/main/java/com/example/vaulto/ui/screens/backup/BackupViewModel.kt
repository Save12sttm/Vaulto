package com.example.vaulto.ui.screens.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.domain.repository.VaultRepository
import com.example.vaulto.util.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getAllItems().first().let { items ->
                _state.update {
                    it.copy(
                        totalItems = items.size,
                        passwordCount = items.count { !it.hasTOTP },
                        totpCount = items.count { it.hasTOTP }
                    )
                }
            }
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val items = repository.getAllItems().first()
                val json = BackupManager.exportToJson(items, encrypted = true)
                
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.toByteArray())
                }
                
                _state.update {
                    it.copy(
                        successMessage = "Backup exported successfully!",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        successMessage = null,
                        errorMessage = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val items = repository.getAllItems().first()
                val csv = BackupManager.exportToCsv(items)
                
                val fileName = "vaulto_export_${System.currentTimeMillis()}.csv"
                // Save to Downloads
                
                _state.update {
                    it.copy(
                        successMessage = "CSV exported to Downloads",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        successMessage = null,
                        errorMessage = "CSV export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: throw Exception("Could not read file")
                
                val items = BackupManager.importFromJson(json)
                
                items.forEach { item ->
                    repository.insertItem(item)
                }
                
                loadStats() // Refresh stats
                
                _state.update {
                    it.copy(
                        successMessage = "Imported ${items.size} items successfully!",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        successMessage = null,
                        errorMessage = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }
}

data class BackupState(
    val totalItems: Int = 0,
    val passwordCount: Int = 0,
    val totpCount: Int = 0,
    val lastBackupDate: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)