package com.example.vaulto.ui.screens.vault

import com.example.vaulto.data.local.entities.VaultItemEntity

data class VaultListState(
    val items: List<VaultItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

data class VaultDetailState(
    val item: VaultItemEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)

data class AddEditState(
    val item: VaultItemEntity? = null,
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val category: String = "General",
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)