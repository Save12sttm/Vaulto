package com.example.vaulto.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaulto.data.local.entities.VaultItemEntity
import com.example.vaulto.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(VaultListState())
    val listState: StateFlow<VaultListState> = _listState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            VaultListState()
        )

    private val _detailState = MutableStateFlow(VaultDetailState())
    val detailState: StateFlow<VaultDetailState> = _detailState.asStateFlow()

    private val _addEditState = MutableStateFlow(AddEditState())
    val addEditState: StateFlow<AddEditState> = _addEditState.asStateFlow()

    init {
        loadAllItems()
    }

    fun loadAllItems() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            repository.getAllItems()
                .catch { e ->
                    _listState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
                .collect { items ->
                    _listState.update { 
                        it.copy(
                            items = items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun searchItems(query: String) {
        _listState.update { it.copy(searchQuery = query) }
        if (query.isEmpty()) {
            loadAllItems()
            return
        }
        
        viewModelScope.launch {
            repository.searchItems(query)
                .catch { e ->
                    _listState.update { it.copy(errorMessage = e.message) }
                }
                .collect { items ->
                    _listState.update { it.copy(items = items) }
                }
        }
    }

    fun loadItemById(id: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            repository.getItemById(id)
                .catch { e ->
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
                .collect { item ->
                    _detailState.update {
                        it.copy(
                            item = item,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun deleteItem(item: VaultItemEntity) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
                _detailState.update { it.copy(showDeleteDialog = false) }
            } catch (e: Exception) {
                _detailState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _detailState.update { it.copy(showDeleteDialog = show) }
    }

    fun loadItemForEdit(id: Long) {
        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            repository.getItemById(id)
                .catch { e ->
                    _addEditState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
                .collect { item ->
                    item?.let {
                        _addEditState.update { state ->
                            state.copy(
                                item = it,
                                title = it.title,
                                username = it.username,
                                password = it.password,
                                url = it.url,
                                notes = it.notes,
                                category = it.category,
                                isFavorite = it.isFavorite,
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    fun updateTitle(title: String) {
        _addEditState.update { it.copy(title = title) }
    }

    fun updateUsername(username: String) {
        _addEditState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        _addEditState.update { it.copy(password = password) }
    }

    fun updateUrl(url: String) {
        _addEditState.update { it.copy(url = url) }
    }

    fun updateNotes(notes: String) {
        _addEditState.update { it.copy(notes = notes) }
    }

    fun updateCategory(category: String) {
        _addEditState.update { it.copy(category = category) }
    }

    fun toggleFavorite() {
        _addEditState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun saveItem() {
        viewModelScope.launch {
            val state = _addEditState.value
            
            if (state.title.isEmpty()) {
                _addEditState.update { it.copy(errorMessage = "Title is required") }
                return@launch
            }

            _addEditState.update { it.copy(isLoading = true) }

            try {
                val item = state.item?.copy(
                    title = state.title,
                    username = state.username,
                    password = state.password,
                    url = state.url,
                    notes = state.notes,
                    category = state.category,
                    isFavorite = state.isFavorite,
                    modifiedAt = System.currentTimeMillis()
                ) ?: VaultItemEntity(
                    title = state.title,
                    username = state.username,
                    password = state.password,
                    url = state.url,
                    notes = state.notes,
                    category = state.category,
                    isFavorite = state.isFavorite
                )

                if (state.item != null) {
                    repository.updateItem(item)
                } else {
                    repository.insertItem(item)
                }

                _addEditState.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _addEditState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun clearAddEditState() {
        _addEditState.value = AddEditState()
    }
}